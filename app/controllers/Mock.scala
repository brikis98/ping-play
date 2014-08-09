package controllers

import play.api.mvc.{Controller, Result, Action}
import scala.concurrent.Future
import play.api.libs.concurrent.Promise
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Instead of calling real remote services, the examples in this app call this mock endpoint, which lets us control
 * the data returned and the service's latency. To keep the examples simple, we return simple strings or numbers as
 * data; obviously, a real service would return something more complicated, like JSON.
 */
object Mock extends Controller {

  def mock(serviceName: String) = Action.async {
    serviceName match {
      case "wvyp" => respond(data = "56", delay = 10)
      case "search" => respond(data = "10", delay = 3000)
      case "likes" => respond(data = "150", delay = 40)
      case "comments" => respond(data = "14", delay = 20)
    }

  }

  private def respond(data: String, delay: Long): Future[Result] = {
    Promise.timeout(Ok(data), delay)
  }
}
