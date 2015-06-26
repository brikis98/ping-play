package data

import scala.concurrent.Future
import play.api.libs.ws.WSClient
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.RequestHeader

/**
 * A dirt-simple client for calling the Mock service endpoint.
 */
class ServiceClient(ws: WSClient) {

  def makeServiceCall(serviceName: String)(implicit request: RequestHeader): Future[String] = {
    ws.url(s"http://localhost:${RequestUtil.port(request)}/mock/$serviceName").get().map(_.body)
  }
}
