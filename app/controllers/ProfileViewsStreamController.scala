package controllers

import data.ServiceClient
import play.api.libs.concurrent.Execution.Implicits._
import ui.HtmlStreamImplicits._
import ui.{ChunkedController, HtmlStream, Pagelet}

/**
 * An example standalone module that represents the "Who's Viewed Your Profile" module from the LinkedIn homepage.
 * It's identical to the ProfileViewsController controller, except that the data is streamed to the browser as soon as it's available in
 * small "pagelets", similar to Facebook's BigPipe.
 */
object ProfileViewsStreamController extends ChunkedController {

  override def stream = HtmlStream {

    val wvypCountFuture = ServiceClient.makeServiceCall("wvyp")
    val searchCountFuture = ServiceClient.makeServiceCall("search")

    val wvypStream = Pagelet.renderStream(wvypCountFuture.map(str => views.html.pagelets.profiles.wvypCount(str.toInt)), "wvypCount")
    val searchStream = Pagelet.renderStream(searchCountFuture.map(str => views.html.pagelets.profiles.searchCount(str.toInt)), "searchCount")
    HtmlStream.interleave(wvypStream, searchStream)
  }

  override def holder(): HtmlStream = views.stream.pagelets.profiles.profile()
}
