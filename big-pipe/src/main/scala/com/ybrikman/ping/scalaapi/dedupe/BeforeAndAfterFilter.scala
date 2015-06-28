package com.ybrikman.ping.scalaapi.dedupe

import play.api.mvc.{Result, RequestHeader, Filter}

import scala.concurrent.{ExecutionContext, Future}

/**
 * A filter that takes two parameters--a before function and an after function--and guarantees the before function is
 * executed before the rest of the filter chain executes and the after function is executed after the rest of the
 * filter chain (no matter what error may have been thrown).
 *
 * @param before
 * @param after
 * @param ec
 */
class BeforeAndAfterFilter(before: RequestHeader => Unit, after: RequestHeader => Unit)(implicit ec: ExecutionContext) extends Filter {
  override def apply(next: (RequestHeader) => Future[Result])(playRequest: RequestHeader): Future[Result] = {
    // Be very careful with error handling to guarantee the after code executes no matter what kind of error happened.
    try {
      before(playRequest)
      next(playRequest).map { result =>
        result.copy(body = result.body.onDoneEnumerating(after(playRequest)))
      }.recover { case t: Throwable =>
        after(playRequest)
        throw t
      }
    } catch {
      case t: Throwable =>
        after(playRequest)
        throw t
    }
  }
}