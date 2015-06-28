package controllers

import play.api.mvc.{Action, Controller}
import data.ServiceClient
import play.api.libs.concurrent.Execution.Implicits._

/**
 * An example standalone module that represents the "Who's Viewed Your Updates" module from the LinkedIn homepage.
 */
class Wvyu(serviceClient: ServiceClient) extends Controller {

  def index(embed: Boolean) = Action.async { implicit request =>
    val likesCountFuture = serviceClient.makeServiceCall("likes")
    val commentsCountFuture = serviceClient.makeServiceCall("comments")

    for {
      likesCount <- likesCountFuture
      commentsCount <- commentsCountFuture
    } yield {
      if (embed) Ok(views.html.wvyu.wvyuBody(likesCount.toInt, commentsCount.toInt))
      else Ok(views.html.wvyu.wvyu(likesCount.toInt, commentsCount.toInt))
    }
  }
}
