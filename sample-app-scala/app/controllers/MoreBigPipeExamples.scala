package controllers

import com.ybrikman.ping.scalaapi.bigpipe.{HtmlStream, Pagelet}
import data.{FakeServiceClient, Response}
import play.api.mvc.{Controller, Action}
import play.api.libs.concurrent.Execution.Implicits._
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamImplicits._
import play.twirl.api.Html
import scala.concurrent.Future

/**
 * A few more BigPipe examples
 * 
 * @param serviceClient
 */
class MoreBigPipeExamples(serviceClient: FakeServiceClient) extends Controller {

  /**
   * Instead of rendering each pagelet server-side with Play's templating, you can send back JSON and render each 
   * pagelet with a client-side templating library such as mustache.js
   * 
   * @return
   */
  def clientSideTemplating = Action {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    val profileFuture = serviceClient.fakeRemoteCallJsonMedium("profile")
    val graphFuture = serviceClient.fakeRemoteCallJsonMedium("graph")
    val feedFuture = serviceClient.fakeRemoteCallJsonSlow("feed")
    val inboxFuture = serviceClient.fakeRemoteCallJsonSlow("inbox")
    val adsFuture = serviceClient.fakeRemoteCallJsonFast("ads")
    val searchFuture = serviceClient.fakeRemoteCallJsonFast("search")

    // Convert each Future into a Pagelet which will send the JSON to the browser as soon as it's available
    val profile = Pagelet.fromJsonFuture(profileFuture, "profile")
    val graph = Pagelet.fromJsonFuture(graphFuture, "graph")
    val feed = Pagelet.fromJsonFuture(feedFuture, "feed")
    val inbox = Pagelet.fromJsonFuture(inboxFuture, "inbox")
    val ads = Pagelet.fromJsonFuture(adsFuture, "ads")
    val search = Pagelet.fromJsonFuture(searchFuture, "search")

    // Compose all the pagelets into an HtmlStream
    val body = HtmlStream.fromInterleavedPagelets(profile, graph, feed, inbox, ads, search)

    // Render the streaming template immediately
    Ok.chunked(views.stream.clientSideTemplating(body))
  }

  /**
   * Shows an example of how to handle an error that occurs part way through streaming a response to the browser. Since
   * you've already sent back the headers with a 200 OK, it's too late to send back a 500 error page, so instead, you
   * have to inject JavaScript into the stream that will show an appropriate error page.
   *
   * @return
   */
  def errorHandling = Action {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow. One of the calls (the feed) will fail with an error.
    val profileFuture = serviceClient.fakeRemoteCallMedium("profile")
    val graphFuture = serviceClient.fakeRemoteCallMedium("graph")
    val feedFuture = serviceClient.fakeRemoteCallErrorSlow("feed")
    val inboxFuture = serviceClient.fakeRemoteCallSlow("inbox")
    val adsFuture = serviceClient.fakeRemoteCallFast("ads")
    val searchFuture = serviceClient.fakeRemoteCallFast("search")

    // Convert each Future into a Pagelet which will be rendered as HTML as soon as the data is available. Note that
    // the render method used here will also handle the case where the Future completes with an error by rendering an
    // error message.
    val profile = Pagelet.fromHtmlFuture(render(profileFuture), "profile")
    val graph = Pagelet.fromHtmlFuture(render(graphFuture), "graph")
    val feed = Pagelet.fromHtmlFuture(render(feedFuture), "feed")
    val inbox = Pagelet.fromHtmlFuture(render(inboxFuture), "inbox")
    val ads = Pagelet.fromHtmlFuture(render(adsFuture), "ads")
    val search = Pagelet.fromHtmlFuture(render(searchFuture), "search")

    // Compose all the pagelets into an HtmlStream
    val body = HtmlStream.fromInterleavedPagelets(profile, graph, feed, inbox, ads, search)

    // Render the streaming template immediately
    Ok.chunked(views.stream.withBigPipe(body))
  }

  /**
   * When the given Future redeems, render it with the module template. If the Future fails, render it with the
   * error template.
   *
   * @param dataFuture
   * @return
   */
  private def render(dataFuture: Future[Response]): Future[Html] = {
    dataFuture.map(views.html.helpers.module.apply).recover { case t: Throwable => views.html.helpers.error(t) }
  }
}
