package controllers

import play.api.mvc.{Cookie, Action, Controller}
import data.ServiceClient
import play.api.libs.concurrent.Execution.Implicits._

object Wvyp extends Controller {

  def index(embed: Boolean) = Action.async {
    val wvypCountFuture = ServiceClient.makeServiceCall("wvyp")
    val searchCountFuture = ServiceClient.makeServiceCall("search")

    for {
      wvypCount <- wvypCountFuture
      searchCount <- searchCountFuture
    } yield {
      if (embed) Ok(views.html.wvyp.wvypBody(wvypCount.toInt, searchCount.toInt)).withCookies(Cookie("foo", "bar"))
      else Ok(views.html.wvyp.wvyp(wvypCount.toInt, searchCount.toInt))
    }
  }
}
