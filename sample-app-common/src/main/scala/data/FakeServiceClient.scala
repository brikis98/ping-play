package data

import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import scala.util.Random

/**
 * A client that represents fake calls to remote backend services.
 */
class FakeServiceClient(futureUtil: FutureUtil) {

  import data.FakeServiceClient._

  def fakeRemoteCallFast(id: String): Future[Response] = fakeRemoteCall(id, FAST_RESPONSE_TIME_IN_MILLIS)
  def fakeRemoteCallMedium(id: String): Future[Response] = fakeRemoteCall(id, MEDIUM_RESPONSE_TIME_IN_MILLIS)
  def fakeRemoteCallSlow(id: String): Future[Response] = fakeRemoteCall(id, SLOW_RESPONSE_TIME_IN_MILLIS)

  def fakeRemoteCall(id: String, delayInMillis: Long): Future[Response] = {
    val randomJitter = new Random().nextInt(delayInMillis.toInt).toLong
    val delay = delayInMillis + randomJitter

    val fakeJsonResponse = Response(id, delay)
    futureUtil.timeout(fakeJsonResponse, delay)
  }
}

object FakeServiceClient {
  val FAST_RESPONSE_TIME_IN_MILLIS = 5
  val MEDIUM_RESPONSE_TIME_IN_MILLIS = 500
  val SLOW_RESPONSE_TIME_IN_MILLIS = 3000
}


