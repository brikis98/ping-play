package data

import scala.concurrent.Future
import play.api.libs.ws.WS
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.RequestHeader

/**
 * A dirt-simple client for calling the Mock service endpoint.
 */
object ServiceClient {

  def makeServiceCall(serviceName: String)(implicit request: RequestHeader): Future[String] = {
    WS.url(s"http://localhost:${RequestUtil.port(request)}/mock/$serviceName").get().map(_.body)
  }
}
