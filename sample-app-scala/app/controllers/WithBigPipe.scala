package controllers

import com.ybrikman.ping.javaapi.bigpipe.PageletRenderOptions
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamImplicits._
import com.ybrikman.ping.scalaapi.bigpipe.{BigPipe, HtmlPagelet}
import data.FakeServiceClient
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, Controller}

/**
 * A page that uses BigPipe style streaming to show you how much faster it is to load.
 * 
 * @param serviceClient
 */
class WithBigPipe(serviceClient: FakeServiceClient) extends Controller {

  def index = Action {
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

    // Use BigPipe to compose the pagelets and render them immediately using a streaming template
    val bigPipe = new BigPipe(PageletRenderOptions.ClientSide, profile, graph, feed, inbox, ads, search)
    Ok.chunked(views.stream.withBigPipe(bigPipe, profile, graph, feed, inbox, ads, search))
  }
}
