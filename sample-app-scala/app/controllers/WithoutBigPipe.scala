package controllers

import data.ServiceClient
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._

/**
 * An example that shows a page that does NOT use BigPipe streaming and how much slower it is to load.
 *
 * @param serviceClient
 */
class WithoutBigPipe(serviceClient: ServiceClient) extends Controller {

  def index = Action.async { implicit request =>
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    val profileFuture = serviceClient.fakeRemoteCallMedium("profile")
    val graphFuture = serviceClient.fakeRemoteCallMedium("graph")
    val feedFuture = serviceClient.fakeRemoteCallSlow("feed")
    val inboxFuture = serviceClient.fakeRemoteCallSlow("inbox")
    val adsFuture = serviceClient.fakeRemoteCallFast("ads")
    val searchFuture = serviceClient.fakeRemoteCallFast("search")

    // Wait for all the remote calls to complete
    for {
      profile <- profileFuture
      graph <- graphFuture
      feed <- feedFuture
      inbox <- inboxFuture
      ads <- adsFuture
      search <- searchFuture
    } yield {
      // Render the template once all the data is available
      Ok(views.html.withoutBigPipe(profile, graph, feed, inbox, ads, search))
    }
  }
}
