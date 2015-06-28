package com.ybrikman.ping.scalaapi.dedupe

import java.util.concurrent.CopyOnWriteArrayList

import org.specs2.mutable.Specification
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Result, RequestHeader, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.collection.JavaConverters._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class TestBeforeAndAfterFilter extends Specification {
  "BeforeAndAfterFilter should" >> {
    "Call the before function once before the rest of the filter chain and the after function once after the rest of the filter chain" >> {
      val events = new CopyOnWriteArrayList[String]()
      val expectedResult = "testing"

      val filter = new BeforeAndAfterFilter(
        before = rh => events.add("before"),
        after = rh => events.add("after"))

      def next(rh: RequestHeader): Future[Result] = {
        events.add("next")
        Future.successful(Results.Ok(expectedResult))
      }

      val actualResult = contentAsString(filter(next _)(FakeRequest()))

      actualResult mustEqual expectedResult
      events.asScala must containTheSameElementsAs(Seq("before", "next", "after"))
    }

    "Call the before and after functions even if the next item in the filter chain throws an exception" >> {
      val events = new CopyOnWriteArrayList[String]()

      val filter = new BeforeAndAfterFilter(
        before = rh => events.add("before"),
        after = rh => events.add("after"))

      def next(rh: RequestHeader): Future[Result] = {
        events.add("next")
        throw new BeforeAndAfterTestException
      }

      contentAsString(filter(next _)(FakeRequest())) must throwA[BeforeAndAfterTestException]
      events.asScala must containTheSameElementsAs(Seq("before", "next", "after"))
    }

    "Call the before and after functions even if the next item in the filter chain returns a failed future" >> {
      val events = new CopyOnWriteArrayList[String]()

      val filter = new BeforeAndAfterFilter(
        before = rh => events.add("before"),
        after = rh => events.add("after"))

      def next(rh: RequestHeader): Future[Result] = {
        events.add("next")
        Future.failed(new BeforeAndAfterTestException)
      }

      contentAsString(filter(next _)(FakeRequest())) must throwA[BeforeAndAfterTestException]
      events.asScala must containTheSameElementsAs(Seq("before", "next", "after"))
    }

    "Call the before and after functions even if the next item in the filter chain returns an Enumerator that is already done" >> {
      val events = new CopyOnWriteArrayList[String]()

      val filter = new BeforeAndAfterFilter(
        before = rh => events.add("before"),
        after = rh => events.add("after"))

      def next(rh: RequestHeader): Future[Result] = {
        events.add("next")
        Future.successful(Results.Ok.chunked(Enumerator.eof[String]))
      }

      contentAsString(filter(next _)(FakeRequest())) mustEqual ""
      events.asScala must containTheSameElementsAs(Seq("before", "next", "after"))
    }

    "Call the before and after functions even if the next item in the filter chain returns an Enumerator that is empty" >> {
      val events = new CopyOnWriteArrayList[String]()

      val filter = new BeforeAndAfterFilter(
        before = rh => events.add("before"),
        after = rh => events.add("after"))

      def next(rh: RequestHeader): Future[Result] = {
        events.add("next")
        Future.successful(Results.Ok.chunked(Enumerator.empty[String]))
      }

      contentAsString(filter(next _)(FakeRequest())) mustEqual ""
      events.asScala must containTheSameElementsAs(Seq("before", "next", "after"))
    }

    "Call the before and after functions even if the next item in the filter chain returns an Enumerator that throws an exception" >> {
      val events = new CopyOnWriteArrayList[String]()

      val filter = new BeforeAndAfterFilter(
        before = rh => events.add("before"),
        after = rh => events.add("after"))

      def next(rh: RequestHeader): Future[Result] = {
        events.add("next")
        Future.successful(Results.Ok.chunked(Enumerator.repeatM[String](throw new BeforeAndAfterTestException)))
      }

      contentAsString(filter(next _)(FakeRequest())) must throwA[BeforeAndAfterTestException]
      events.asScala must containTheSameElementsAs(Seq("before", "next", "after"))
    }

    "Call the before and after functions even if the next item in the filter chain returns an Enumerator built from a failed Future" >> {
      val events = new CopyOnWriteArrayList[String]()

      val filter = new BeforeAndAfterFilter(
        before = rh => events.add("before"),
        after = rh => events.add("after"))

      def next(rh: RequestHeader): Future[Result] = {
        events.add("next")
        Future.successful(Results.Ok.chunked(Enumerator.repeatM[String](Future.failed(new BeforeAndAfterTestException))))
      }

      contentAsString(filter(next _)(FakeRequest())) must throwA[BeforeAndAfterTestException]
      events.asScala must containTheSameElementsAs(Seq("before", "next", "after"))
    }

  }
}

class BeforeAndAfterTestException extends RuntimeException