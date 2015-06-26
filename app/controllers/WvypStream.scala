package controllers

import play.api.mvc.{Action, Controller}
import data.ServiceClient
import ui.{HtmlStream, Pagelet}
import ui.HtmlStreamImplicits._
import play.api.libs.concurrent.Execution.Implicits._

/**
 * An example standalone module that represents the "Who's Viewed Your Profile" module from the LinkedIn homepage.
 * It's identical to the Wvyp controller, except that the data is streamed to the browser as soon as it's available in
 * small "pagelets", similar to Facebook's BigPipe.
 */
class WvypStream(serviceClient: ServiceClient) extends Controller {

  def index = Action { implicit request =>
    val wvypCountFuture = serviceClient.makeServiceCall("wvyp")
    val searchCountFuture = serviceClient.makeServiceCall("search")

    val wvypStream = Pagelet.renderStream(wvypCountFuture.map(str => views.html.wvyp.wvypCount(str.toInt)), "wvypCount")
    val searchStream = Pagelet.renderStream(searchCountFuture.map(str => views.html.wvyp.searchCount(str.toInt)), "searchCount")

    val body = HtmlStream.interleave(wvypStream, searchStream)

    Ok.chunked(views.stream.wvyp.wvyp(body))
  }
}
