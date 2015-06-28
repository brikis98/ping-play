package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import data.ServiceClient
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Execution.Implicits._

class WvypEnumerator(serviceClient: ServiceClient) extends Controller {

  def index = Action { implicit request =>
    val wyvpCountFuture = serviceClient.makeServiceCall("wvyp")
    val searchCountFuture = serviceClient.makeServiceCall("search")

    val wvypCountEnum = Enumerator.flatten(wyvpCountFuture.map(str => Enumerator(str + "\n")))
    val searchCountEnum = Enumerator.flatten(searchCountFuture.map(str => Enumerator(str + "\n")))

    val body = wvypCountEnum.andThen(searchCountEnum)

    Ok.chunked(body)
  }
}