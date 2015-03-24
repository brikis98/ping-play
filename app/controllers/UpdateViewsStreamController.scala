package controllers

import data.ServiceClient
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import ui.HtmlStreamImplicits._
import ui.{ChunkedController, HtmlStream, Pagelet}

/**
 * An example standalone module that represents the "Who's Viewed Your Profile" module from the LinkedIn homepage.
 * It's identical to the ProfileViewsController controller, except that the data is streamed to the browser as soon as it's available in
 * small "pagelets", similar to Facebook's BigPipe.
 */
object UpdateViewsStreamController  extends ChunkedController {

  override def stream = HtmlStream {

    val likesCountFuture = ServiceClient.makeServiceCall("likes")
    val commentsCountFuture = ServiceClient.makeServiceCall("comments")

    val likesStream =Pagelet.renderStream(likesCountFuture.map(str => views.html.pagelets.updates.likesCount(str.toInt)), "likeCount")
    HtmlStream.interleave(likesStream)
  }

  override def holder(): HtmlStream = views.stream.pagelets.updates.updates()
}
