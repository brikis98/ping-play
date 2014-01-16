package controllers

import play.api.mvc.{Controller, SimpleResult, Action}
import scala.concurrent.Future
import play.api.libs.concurrent.Promise
import play.api.libs.concurrent.Execution.Implicits._

object Mock extends Controller {

  def mock(serviceName: String) = Action.async {
    serviceName match {
      case "wvyp" => respond(data = "56", delay = 10)
      case "search" => respond(data = "10", delay = 3000)
      case "likes" => respond(data = "150", delay = 40)
      case "comments" => respond(data = "14", delay = 20)
    }

  }

  private def respond(data: String, delay: Long): Future[SimpleResult] = {
    Promise.timeout(Ok(data), delay)
  }
}
