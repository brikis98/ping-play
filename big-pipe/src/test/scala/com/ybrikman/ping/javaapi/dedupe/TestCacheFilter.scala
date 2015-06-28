package com.ybrikman.ping.javaapi.dedupe

import java.util.function.Supplier

import com.ybrikman.ping.scalaapi.dedupe.CacheNotInitializedException
import org.specs2.mutable.Specification
import play.api.mvc.{Results, Result, RequestHeader}
import play.api.test.FakeRequest
import play.mvc.Http.{RequestBuilder, Context}
import play.api.test.Helpers._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class TestCacheFilter extends Specification {
  "The Java CacheFilter should" >> {
    "initialize the cache before the filter chain and clean it up after the filter chain" >> {
      val fakeContext = new Context(new RequestBuilder)
      val cache = new DedupingCache[String, String]
      val filter = new CacheFilter(cache, defaultContext, fakeContext)
      val expectedResult = "bar"

      def next(rh: RequestHeader): Future[Result] = {
        Future.successful(Results.Ok(cache.get("foo", supplier(expectedResult), fakeContext)))
      }

      val fakeRequest = FakeRequest()
      val actualResult = contentAsString(filter(next _)(fakeRequest))
      actualResult mustEqual expectedResult

      // Ensure cache was cleaned up for that request
      cache.get("foo", supplier("shouldNotBeUsed"), fakeContext) must throwA[CacheNotInitializedException]
    }

    "initialize the cache before the filter chain and clean it up after the filter chain even if an exception is thrown" >> {
      val fakeContext = new Context(new RequestBuilder)
      val cache = new DedupingCache[String, String]
      val filter = new CacheFilter(cache, defaultContext, fakeContext)
      val expectedResult = "bar"

      def next(rh: RequestHeader): Future[Result] = {
        throw new CacheFilterTestException(cache.get("foo", supplier(expectedResult), fakeContext))
      }

      val fakeRequest = FakeRequest()
      contentAsString(filter(next _)(fakeRequest)) must throwA[CacheFilterTestException](message = expectedResult)

      // Ensure cache was cleaned up for that request
      cache.get("foo", supplier("shouldNotBeUsed"), fakeContext) must throwA[CacheNotInitializedException]
    }
  }

  private def supplier[A](value: => A): Supplier[A] = {
    new Supplier[A] {
      override def get(): A = value
    }
  }
}

class CacheFilterTestException(message: String) extends RuntimeException(message)