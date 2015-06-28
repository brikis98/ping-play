package com.ybrikman.ping.scalaapi.dedupe

import java.util.concurrent.atomic.AtomicInteger

import org.specs2.mutable.Specification
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest

class TestDedupingCache extends Specification {
  "The Scala DedupingCache get method should" >> {

    "throw an exception if the cache is not initialized" >> {
      val uninitializedCache = new DedupingCache[String, Integer]
      uninitializedCache.get("foo", 1)(FakeRequest()) must throwA[CacheNotInitializedException]
    }

    "return valueIfMissing when the cache is empty" >> {
      implicit val fakeRequest = FakeRequest()
      val cache = createInitializedCache[String, Int]
      val value = new AtomicInteger(0)

      cache.get("foo", value.incrementAndGet()) mustEqual 1
    }

    "store different values for different keys" >> {
      implicit val fakeRequest = FakeRequest()
      val cache = createInitializedCache[String, Int]

      val value1 = new AtomicInteger(0)
      cache.get("foo", value1.incrementAndGet()) mustEqual 1

      val value2 = new AtomicInteger(100)
      cache.get("bar", value2.incrementAndGet()) mustEqual 101

      // Recheck to make sure the later calls to get had no effect on the previous ones
      cache.get("foo", value1.incrementAndGet()) mustEqual 1
      cache.get("bar", value2.incrementAndGet()) mustEqual 101
    }

    "store different values for different requests" >> {
      val cache = new DedupingCache[String, Int]

      val fakeRequest1 = FakeRequest().copy(id = 123)
      val fakeRequest2 = FakeRequest().copy(id = 456)

      cache.initCacheForRequest(fakeRequest1)
      cache.initCacheForRequest(fakeRequest2)

      val value1 = new AtomicInteger(0)
      cache.get("foo", value1.incrementAndGet())(fakeRequest1) mustEqual 1

      val value2 = new AtomicInteger(100)
      cache.get("foo", value2.incrementAndGet())(fakeRequest2) mustEqual 101

      // Recheck to make sure the later calls to get had no effect on the previous ones
      cache.get("foo", value1.incrementAndGet())(fakeRequest1) mustEqual 1
      cache.get("foo", value2.incrementAndGet())(fakeRequest2) mustEqual 101
    }


    "only call valueIfMissing once, no matter how many times we call get on the same key" >> {
      implicit val fakeRequest = FakeRequest()
      val cache = createInitializedCache[String, Int]
      val value = new AtomicInteger(0)

      cache.get("foo", value.incrementAndGet()) mustEqual 1
      cache.get("foo", value.incrementAndGet()) mustEqual 1
      cache.get("foo", value.incrementAndGet()) mustEqual 1
    }

    "not call valueIfMissing at all if the key is already in the cache" >> {
      implicit val fakeRequest = FakeRequest()
      val cache = createInitializedCache[String, Int]

      val originalValue = new AtomicInteger(0)
      cache.get("foo", originalValue.incrementAndGet()) mustEqual 1

      val newValue = new AtomicInteger(100)
      cache.get("foo", newValue.incrementAndGet()) mustEqual 1
    }

    "throw an exception if the cache has been cleaned up for the current request" >> {
      implicit val fakeRequest = FakeRequest()
      val cache = createInitializedCache[String, Int]
      val value = new AtomicInteger(0)

      cache.get("foo", value.incrementAndGet()) mustEqual 1

      cache.cleanupCacheForRequest(fakeRequest)

      cache.get("foo", 1) must throwA[CacheNotInitializedException]
    }
  }

  private def createInitializedCache[K, V](implicit request: RequestHeader): DedupingCache[K, V] = {
    val cache = new DedupingCache[K, V]
    cache.initCacheForRequest(request)
    cache
  }
}
