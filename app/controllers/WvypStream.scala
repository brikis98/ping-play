package controllers

import play.api.mvc.{Action, Controller}
import data.ServiceClient
import ui.{HtmlStream, Pagelet}
import ui.HtmlStreamImplicits._
import play.api.libs.concurrent.Execution.Implicits._

object WvypStream extends Controller {

  def index = Action {

    val wvypCountFuture = ServiceClient.makeServiceCall("wvyp")
    val searchCountFuture = ServiceClient.makeServiceCall("search")

    val wvypStream = Pagelet.renderStream(wvypCountFuture.map(str => views.html.wvyp.wvypCount(str.toInt)), "wvypCount")
    val searchStream = Pagelet.renderStream(searchCountFuture.map(str => views.html.wvyp.searchCount(str.toInt)), "searchCount")

    val body = HtmlStream.interleave(wvypStream, searchStream)

    Ok.feed(views.stream.wvyp.wvyp(body))
  }
}
