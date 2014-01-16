package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._
import ui.Pagelet

object Aggregator extends Controller {

  def index = Action.async { request =>
    val wvypFuture = Wvyp.index(embed = true)(request)
    val wvyuFuture = Wvyu.index(embed = true)(request)

    for {
      wvyp <- wvypFuture
      wvyu <- wvyuFuture

      wvypBody <- Pagelet.readBody(wvyp)
      wvyuBody <- Pagelet.readBody(wvyu)
    } yield {
      Ok(views.html.aggregator.aggregator(wvypBody, wvyuBody)).withCookies(Pagelet.mergeCookies(wvyp, wvyu):_*)
    }
  }
}
