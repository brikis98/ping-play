package controllers

import com.ybrikman.ping.scalaapi.bigpipe.{HtmlStream, Pagelet}
import data.FakeServiceClient
import play.api.mvc.{Controller, Action}
import play.api.libs.concurrent.Execution.Implicits._
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamImplicits._

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
}
