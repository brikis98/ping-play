package com.ybrikman.ping

import com.ybrikman.ping.TimingHelper._
import com.ybrikman.ping.CustomRoutes._
import com.ybrikman.ping.javaapi.bigpipe.PageletRenderOptions
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamImplicits._
import com.ybrikman.ping.scalaapi.bigpipe._
import data.FutureUtil
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Results}
import play.api.routing.Router
import play.api.routing.sird._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Tests that BigPipe is actually streaming data as soon as it's available and that chunks are not blocked anywhere.
 * The Scala and Java sample apps can extend this trait to run all the tests in it.
 */
trait BaseBigPipeTimingSpec extends PingSpecification {
  "BigPipe streaming" should {
    "Send down the data in-order, only after all of it is available, without BigPipe" in new WithWarmedUpPingTestServer(createTestComponents(withRouterToTestTimings)) {
      val chunkTimings = getTimings(components.wsClient, s"http://localhost:$port/withoutBigPipe")
      chunkTimings must not be empty

      // Time-to-first-byte: make sure the first chunk was sent back after the maxDelay (within a tolerance of
      // ToleranceInMillis)
      val firstChunk = chunkTimings(0)
      firstChunk.content mustEqual FirstChunkContent
      firstChunk.timeElapsed must beGreaterThan(maxDelay - ToleranceInMillis) and beLessThan(maxDelay + ToleranceInMillis)

      // Make sure the contents for each pagelet were sent back exactly once
      val pageletContentTiming = PageletIndices.flatMap { index =>
        chunkTimings.filter(_.content.contains(content(index)))
      }
      pageletContentTiming must have size PageletIndices.size

      // Check that contents for each pagelet were delayed by the slowest pagelet and no more (within a tolerance of
      // ToleranceInMillis)
      val expectedTimingMatchers = PageletIndices.map { index =>
        beGreaterThan(maxDelay - ToleranceInMillis) and beLessThan(maxDelay + ToleranceInMillis)
      }
      pageletContentTiming.map(_.timeElapsed) must contain(eachOf(expectedTimingMatchers:_*)).inOrder
    }

    "Send down the data out-of-order, as soon as any of it is available, with client-side streaming" in new WithWarmedUpPingTestServer(createTestComponents(withRouterToTestTimings)) {
      val chunkTimings = getTimings(components.wsClient, s"http://localhost:$port/withBigPipeClientSide")
      chunkTimings must not be empty

      // Time-to-first-byte: make sure the first chunk was sent back almost immediately
      val firstChunk = chunkTimings(0)
      firstChunk.content mustEqual FirstChunkContent
      firstChunk.timeElapsed must beLessThan(ToleranceInMillis)

      // Placeholders: make sure all the placeholders were sent back almost immediately and exactly once
      val pageletPlaceholderTiming = PageletIndices.flatMap { index =>
        chunkTimings.filter(_.content.contains(placeholder(id(index))))
      }
      pageletPlaceholderTiming must have size PageletIndices.size
      pageletPlaceholderTiming.map(_.timeElapsed) must contain(beLessThan(ToleranceInMillis)).forall

      // Make sure the contents for each pagelet were sent back exactly once
      val pageletContentTiming = PageletIndices.flatMap { index =>
        chunkTimings.filter(_.content.contains(content(index)))
      }
      pageletContentTiming must have size PageletIndices.size

      // Check that contents for each pagelet were delayed by no more and no less than
      // DELAY_MULTIPLIER_IN_MILLIS (within a tolerance of ToleranceInMillis)
      val expectedTimingMatchers = PageletIndices.map { index =>
        val expecteDelay = delay(index)
        beGreaterThan(expecteDelay - ToleranceInMillis) and beLessThan(expecteDelay + ToleranceInMillis)
      }
      pageletContentTiming.map(_.timeElapsed) must contain(eachOf(expectedTimingMatchers:_*)).inOrder
    }

    "Send down the data in-order, as soon as it's available, with server-side streaming" in new WithWarmedUpPingTestServer(createTestComponents(withRouterToTestTimings)) {
      val chunkTimings = getTimings(components.wsClient, s"http://localhost:$port/withBigPipeServerSide")
      chunkTimings must not be empty

      // Time-to-first-byte: make sure the first chunk was sent back almost immediately
      val firstChunk = chunkTimings(0)
      firstChunk.content mustEqual FirstChunkContent
      firstChunk.timeElapsed must beLessThan(ToleranceInMillis)

      // Make sure the contents for each pagelet were sent back exactly once
      val pageletContentTiming = PageletIndices.flatMap { index =>
        chunkTimings.filter(_.content.contains(content(index)))
      }
      pageletContentTiming must have size PageletIndices.size

      // Check that contents for each pagelet were delayed by the slowest pagelet and no more (within a tolerance of
      // ToleranceInMillis)
      val expectedTimingMatchers = PageletIndices.map { index =>
        beGreaterThan(maxDelay - ToleranceInMillis) and beLessThan(maxDelay + ToleranceInMillis)
      }
      pageletContentTiming.map(_.timeElapsed) must contain(eachOf(expectedTimingMatchers:_*)).inOrder
    }
  }

  private def getTimings(wsClient: WSClient, url: String): Seq[Timing] = {
    val initialTimings = Timings()
    val (_, bodyEnumerator) = await(wsClient.url(url).getStream())

    val checkTiming = Iteratee.fold[Array[Byte], Timings](initialTimings) { (timings, chunk) =>
      timings.addChunk(chunk)
    }

    val timings = await(bodyEnumerator.run(checkTiming))

    // Useful for debugging
    println(s"Timings and content for url $url:\n")
    timings.chunkTimings.foreach(timing => println(s"----- ${timing.timeElapsed} ms -----\n\n${timing.content}\n\n"))

    timings.chunkTimings
  }
}

object TimingHelper {
  val PageletIndices = 1 until 5

  // Each pagelet id will have this prefix to make it easier to find in the stream of data
  val IdPrefix = "pagelet_id_"

  // The contents of each pagelet will have this prefix to make it easier to find in the stream of data
  val ContentPrefix = "pagelet_content_"

  // The placeholder for each pagelet will have this prefix to make it easier to find in the stream of data
  val PlaceHolderPrefix = "pagelet_placeholder_"

  // The contents of the very first chunk that should be sent back by each page
  val FirstChunkContent = "first_chunk_content"

  // Each pagelet will be delayed by this many milliseconds
  val DelayMultiplierInMillis = 3000L

  // With tests running in parallel, things may get a bit delayed, so check all timings within this tolerance
  val ToleranceInMillis = DelayMultiplierInMillis / 10

  def id(index: Int): String = {
    s"$IdPrefix$index"
  }

  def content(index: Int): String = {
    s"$ContentPrefix$index"
  }

  def delay(index: Int): Long = {
    (PageletIndices.size - index) * DelayMultiplierInMillis
  }

  def maxDelay: Long = {
    delay(PageletIndices.head)
  }

  def placeholder(id: String): String = {
    s"$PlaceHolderPrefix$id"
  }
}

case class Timings(startTime: Long = System.currentTimeMillis(), chunkTimings: Seq[Timing] = Seq.empty) {
  def addChunk(contents: Array[Byte]): Timings = {
    val timeElapsed = System.currentTimeMillis() - startTime
    copy(chunkTimings = chunkTimings :+ Timing(new String(contents, "UTF-8"), timeElapsed))
  }
}

case class Timing(content: String, timeElapsed: Long)

class MockTextPagelet(id: String, content: Future[String]) extends TextPagelet(id, content) {
  override def renderPlaceholder(implicit ec: ExecutionContext): HtmlStream = {
    HtmlStream.fromHtml(com.ybrikman.bigpipe.html.pageletServerSide(placeholder(id), PageletConstants.EmptyContent))
  }
}

object CustomRoutes {
  def withRouterToTestTimings: Option[RouterComponents => Router] = {
    def createRoutes(routerComponents: RouterComponents): Router = {
      val futureUtil = new FutureUtil(routerComponents.actorSystem)

      Router.from {
        case GET(p"/withoutBigPipe") => Action.async {
          val futures = mockRemoteServiceCalls(futureUtil).map(_._2)
          Future.sequence(futures).map { contents =>
            Results.Ok.chunked(Enumerator(FirstChunkContent).andThen(Enumerator(contents:_*)))
          }
        }
        case GET(p"/withBigPipeClientSide") => Action {
          val pagelets = mockRemoteServiceCalls(futureUtil).map { case (id, data) => new MockTextPagelet(id, data) }
          Results.Ok.chunked(renderPagelets(PageletRenderOptions.ClientSide, pagelets))
        }
        case GET(p"/withBigPipeServerSide") => Action {
          val pagelets = mockRemoteServiceCalls(futureUtil).map { case (id, data) => new MockTextPagelet(id, data) }
          Results.Ok.chunked(renderPagelets(PageletRenderOptions.ServerSide, pagelets))
        }
        case GET(p"/warmup") => Action {
          Results.Ok("warmup")
        }
      }
    }

    Option(createRoutes _)
  }

  // Generate a series of Futures that represent remote calls. The Futures are returned in reverse order, from slowest
  // to fastest, as a way to demonstrate the advantages of out-of-order client-side rendering.
  private def mockRemoteServiceCalls(futureUtil: FutureUtil): Seq[(String, Future[String])] = {
    PageletIndices.map { index =>
      id(index) -> futureUtil.timeout(content(index), delay(index))
    }
  }

  private def renderPagelets(renderOptions: PageletRenderOptions, pagelets: Seq[Pagelet]): HtmlStream = {
    val bigPipe = new BigPipe(renderOptions, pagelets:_*)
    bigPipe.render { renderedPagelets =>
      pagelets.foldLeft(HtmlStream.fromString(FirstChunkContent)) { (stream, pagelet) =>
        stream.andThen(renderedPagelets(pagelet.id))
      }
    }
  }
}
