package controllers

import com.ybrikman.ping.scalaapi.bigpipe.{HtmlStream, Pagelet}
import data.ServiceClient
import play.api.mvc.{Controller, Action}
import play.api.libs.concurrent.Execution.Implicits._
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamImplicits._

/**
 * A page that uses BigPipe style streaming to show you how much faster it is to load.
 * 
 * @param serviceClient
 */
class WithBigPipe(serviceClient: ServiceClient) extends Controller {

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
    val profile = Pagelet.fromHtmlFuture(profileFuture.map(views.html.helpers.module.apply), "profile")
    val graph = Pagelet.fromHtmlFuture(graphFuture.map(views.html.helpers.module.apply), "graph")
    val feed = Pagelet.fromHtmlFuture(feedFuture.map(views.html.helpers.module.apply), "feed")
    val inbox = Pagelet.fromHtmlFuture(inboxFuture.map(views.html.helpers.module.apply), "inbox")
    val ads = Pagelet.fromHtmlFuture(adsFuture.map(views.html.helpers.module.apply), "ads")
    val search = Pagelet.fromHtmlFuture(searchFuture.map(views.html.helpers.module.apply), "search")

    // Compose all the pagelets into an HtmlStream
    val body = HtmlStream.fromInterleavedPagelets(profile, graph, feed, inbox, ads, search)

    // Render the streaming template immediately
    Ok.chunked(views.stream.withBigPipe(body))
  }
}
