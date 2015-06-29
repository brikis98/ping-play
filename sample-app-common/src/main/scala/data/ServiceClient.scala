package data

import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import scala.util.Random

/**
 * A client that represents fake calls to remote backend services.
 */
class ServiceClient(futureUtil: FutureUtil) {

  import data.ServiceClient._

  def fakeRemoteCallFast(id: String): Future[Response] = fakeRemoteCall(id, FAST_RESPONSE_TIME_IN_MILLIS)
  def fakeRemoteCallMedium(id: String): Future[Response] = fakeRemoteCall(id, MEDIUM_RESPONSE_TIME_IN_MILLIS)
  def fakeRemoteCallSlow(id: String): Future[Response] = fakeRemoteCall(id, SLOW_RESPONSE_TIME_IN_MILLIS)

  def fakeRemoteCall(id: String, delayInMillis: Long): Future[Response] = {
    val randomJitter = new Random().nextInt((delayInMillis / 2).toInt).toLong
    val delay = delayInMillis + randomJitter

    val fakeJsonResponse = Response(id, delay)
    futureUtil.timeout(fakeJsonResponse, delay)
  }
}

object ServiceClient {
  val FAST_RESPONSE_TIME_IN_MILLIS = 3
  val MEDIUM_RESPONSE_TIME_IN_MILLIS = 300
  val SLOW_RESPONSE_TIME_IN_MILLIS = 3000
}


