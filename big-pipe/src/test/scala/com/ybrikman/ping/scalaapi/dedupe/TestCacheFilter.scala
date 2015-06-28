package com.ybrikman.ping.scalaapi.dedupe

import org.specs2.mutable.Specification
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Results, Result, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class TestCacheFilter extends Specification {
  "The CacheFilter should" >> {
    "initialize the cache before the filter chain and clean it up after the filter chain" >> {
      val cache = new DedupingCache[String, String]
      val filter = new CacheFilter(cache)
      val expectedResult = "bar"

      def next(rh: RequestHeader): Future[Result] = {
        Future.successful(Results.Ok(cache.get("foo", expectedResult)(rh)))
      }

      val fakeRequest = FakeRequest()
      val actualResult = contentAsString(filter(next _)(fakeRequest))
      actualResult mustEqual expectedResult

      // Ensure cache was cleaned up for that request
      cache.get("foo", "shouldNotBeUsed")(fakeRequest) must throwA[CacheNotInitializedException]
    }

    "initialize the cache before the filter chain and clean it up after the filter chain even if an exception is thrown" >> {
      val cache = new DedupingCache[String, String]
      val filter = new CacheFilter(cache)
      val expectedResult = "bar"

      def next(rh: RequestHeader): Future[Result] = {
        throw new CacheFilterTestException(cache.get("foo", expectedResult)(rh))
      }

      val fakeRequest = FakeRequest()
      contentAsString(filter(next _)(fakeRequest)) must throwA[CacheFilterTestException](message = expectedResult)

      // Ensure cache was cleaned up for that request
      cache.get("foo", "shouldNotBeUsed")(fakeRequest) must throwA[CacheNotInitializedException]
    }
  }
}

class CacheFilterTestException(message: String) extends RuntimeException(message)