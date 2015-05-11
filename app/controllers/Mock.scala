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

  val DEFAULT_WVYP_RESPONSE = "56"
  val DEFAULT_SEARCH_RESPONSE = "10"
  val DEFAULT_LIKES_RESPONSE = "150"
  val DEFAULT_COMMENTS_RESPONSE = "14"

  val FAST_RESPONSE_TIME = 10
  val SLOW_RESPONSE_TIME = 3000

  def mock(serviceName: String) = Action.async {
    serviceName match {
      case "wvyp" => respond(data = DEFAULT_WVYP_RESPONSE, delay = FAST_RESPONSE_TIME)
      case "search" => respond(data = DEFAULT_SEARCH_RESPONSE, delay = SLOW_RESPONSE_TIME)
      case "likes" => respond(data = DEFAULT_LIKES_RESPONSE, delay = FAST_RESPONSE_TIME)
      case "comments" => respond(data = DEFAULT_COMMENTS_RESPONSE, delay = FAST_RESPONSE_TIME)
    }

  }

  private def respond(data: String, delay: Long): Future[Result] = {
    Promise.timeout(Ok(data), delay)
  }
}
