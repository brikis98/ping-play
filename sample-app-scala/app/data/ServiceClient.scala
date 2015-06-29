package data

import com.ybrikman.ping.scalaapi.dedupe.DedupingCache
import play.api.libs.ws.{WSResponse, WSClient}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.RequestHeader

import scala.concurrent.Future

class ServiceClient(ws: WSClient, cache: DedupingCache[String, Future[WSResponse]]) {

  def remoteCall(url: String)(implicit rh: RequestHeader): Future[WSResponse] = {
    cache.get(url, ws.url(url).get())
  }
}
