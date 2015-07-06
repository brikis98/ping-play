package controllers

import com.ybrikman.ping.javaapi.bigpipe.PageletRenderOptions
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamImplicits._
import com.ybrikman.ping.scalaapi.bigpipe._
import data.{FakeServiceClient, Response}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html

import scala.concurrent.Future

/**
 * A few more BigPipe examples
 * 
 * @param serviceClient
 */
class MoreBigPipeExamples(serviceClient: FakeServiceClient) extends Controller {

  /**
   * Renders the exact same page as WithBigPipe#index, but this time with server-side rendering. This will render all
   * pagelets server-side and send them down in-order. The page load time will be longer than with out-of-order
   * client-side rendering (albeit still faster than not using BigPipe at all), but the advantage is that server-side
   * rendering does not depend on JavaScript, which is important for certain use cases (e.g. older browsers, search
   * engine crawlers, SEO).
   *
   * @return
   */
  def serverSideRendering = Action {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    val profileFuture = serviceClient.fakeRemoteCallMedium("profile")
    val graphFuture = serviceClient.fakeRemoteCallMedium("graph")
    val feedFuture = serviceClient.fakeRemoteCallSlow("feed")
    val inboxFuture = serviceClient.fakeRemoteCallSlow("inbox")
    val adsFuture = serviceClient.fakeRemoteCallFast("ads")
    val searchFuture = serviceClient.fakeRemoteCallFast("search")

    // Convert each Future into a Pagelet which will be rendered as HTML as soon as the data is available
    val profile = HtmlPagelet("profile", profileFuture.map(views.html.helpers.module.apply))
    val graph = HtmlPagelet("graph", graphFuture.map(views.html.helpers.module.apply))
    val feed = HtmlPagelet("feed", feedFuture.map(views.html.helpers.module.apply))
    val inbox = HtmlPagelet("inbox", inboxFuture.map(views.html.helpers.module.apply))
    val ads = HtmlPagelet("ads", adsFuture.map(views.html.helpers.module.apply))
    val search = HtmlPagelet("search", searchFuture.map(views.html.helpers.module.apply))

    // Use BigPipe to compose the pagelets and render them immediately using a streaming template. Note that we're using
    // ServerSide rendering in this case.
    val bigPipe = new BigPipe(PageletRenderOptions.ServerSide, profile, graph, feed, inbox, ads, search)
    Ok.chunked(views.stream.withBigPipe(bigPipe, profile, graph, feed, inbox, ads, search))
  }

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
    val profile = JsonPagelet("profile", profileFuture)
    val graph = JsonPagelet("graph", graphFuture)
    val feed = JsonPagelet("feed", feedFuture)
    val inbox = JsonPagelet("inbox", inboxFuture)
    val ads = JsonPagelet("ads", adsFuture)
    val search = JsonPagelet("search", searchFuture)

    // Use BigPipe to compose the pagelets and render them immediately using a streaming template
    val bigPipe = new BigPipe(PageletRenderOptions.ClientSide, profile, graph, feed, inbox, ads, search)
    Ok.chunked(views.stream.clientSideTemplating(bigPipe, profile, graph, feed, inbox, ads, search))
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
    val profile = HtmlPagelet("profile", render(profileFuture))
    val graph = HtmlPagelet("graph", render(graphFuture))
    val feed = HtmlPagelet("feed", render(feedFuture))
    val inbox = HtmlPagelet("inbox", render(inboxFuture))
    val ads = HtmlPagelet("ads", render(adsFuture))
    val search = HtmlPagelet("search", render(searchFuture))

    // Use BigPipe to compose the pagelets and render them immediately using a streaming template
    val bigPipe = new BigPipe(PageletRenderOptions.ClientSide, profile, graph, feed, inbox, ads, search)
    Ok.chunked(views.stream.withBigPipe(bigPipe, profile, graph, feed, inbox, ads, search))
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
