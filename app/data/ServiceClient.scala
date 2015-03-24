package data

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WS

import scala.concurrent.Future

/**
 * A dirt-simple client for calling the Mock service endpoint.
 */
object ServiceClient {

  def makeServiceCall(serviceName: String): Future[String] = {
    val port = Play.current.configuration.getString("http.port").getOrElse("9000")
    WS.url(s"http://localhost:$port/mock/$serviceName").get().map(_.body)
  }
}
