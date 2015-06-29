package controllers

import data.{UrlAndId, ServiceClient}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._

/**
 * This controller shows an example of how remote service calls can be transparently de-duped using the DedupingCache
 * to ensure that we only make one remote call for each unique URL.
 *
 * @param serviceClient
 */
class Deduping(serviceClient: ServiceClient) extends Controller {

  def index = Action.async { implicit request =>
    // Call an endpoint on this same Play app that returns the request id, which should be unique for every incoming
    // request
    val url1 = s"http://${request.host}/mock/requestId"
    val url2 = s"http://${request.host}/mock/requestId?foo=bar"

    // Thanks to the DedupingCache in the ServiceClient, all 3 calls to url1 will result in only a single remote call
    // and the call to url2 will result in a separate call
    val future1 = serviceClient.remoteCall(url1)
    val future2 = serviceClient.remoteCall(url1)
    val future3 = serviceClient.remoteCall(url1)
    val future4 = serviceClient.remoteCall(url2)

    for {
      result1 <- future1
      result2 <- future2
      result3 <- future3
      result4 <- future4
    } yield {
      // We should expect to see the same request id for the first 3 requests (since deduping should ensure only one
      // request is actually made) and a different id for the fourth one
      Ok(views.html.dedupe(UrlAndId(url1, result1.body), UrlAndId(url1, result2.body), UrlAndId(url1, result3.body), UrlAndId(url2, result4.body)))
    }
  }
}
