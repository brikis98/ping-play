package data

import scala.concurrent.Future
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._

/**
 * A dirt-simple client for calling the Mock service endpoint.
 */
object ServiceClient {

  def makeServiceCall(serviceName: String): Future[String] = {
    WS.url(s"http://localhost:9000/mock/$serviceName").get().map(_.body)
  }
}
