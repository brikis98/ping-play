package com.ybrikman.ping.javaapi.dedupe

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

import com.ybrikman.ping.scalaapi.dedupe.CacheNotInitializedException
import org.specs2.mutable.Specification
import play.mvc.Http.{RequestBuilder, Context}

class TestDedupingCache extends Specification {
  "The Java DedupingCache get method should" >> {

    "throw an exception if the cache is not initialized" >> {
      val uninitializedCache = new DedupingCache[String, Integer]
      val fakeContext = new Context(new RequestBuilder)
      uninitializedCache.get("foo", supplier(1), fakeContext) must throwA[CacheNotInitializedException]
    }

    "return valueIfMissing when the cache is empty" >> {
      val fakeContext = new Context(new RequestBuilder)
      val cache = createInitializedCache[String, Int](fakeContext)
      val value = new AtomicInteger(0)

      cache.get("foo", supplier(value.incrementAndGet()), fakeContext) mustEqual 1
    }

    "store different values for different keys" >> {
      val fakeContext = new Context(new RequestBuilder)
      val cache = createInitializedCache[String, Int](fakeContext)

      val value1 = new AtomicInteger(0)
      cache.get("foo", supplier(value1.incrementAndGet()), fakeContext) mustEqual 1

      val value2 = new AtomicInteger(100)
      cache.get("bar", supplier(value2.incrementAndGet()), fakeContext) mustEqual 101

      // Recheck to make sure the later calls to get had no effect on the previous ones
      cache.get("foo", supplier(value1.incrementAndGet()), fakeContext) mustEqual 1
      cache.get("bar", supplier(value2.incrementAndGet()), fakeContext) mustEqual 101
    }

    "store different values for different requests" >> {
      val cache = new DedupingCache[String, Int]

      val fakeContext1 = new Context(new RequestBuilder().id(123L))
      val fakeContext2 = new Context(new RequestBuilder().id(456L))

      cache.initCacheForRequest(fakeContext1)
      cache.initCacheForRequest(fakeContext2)

      val value1 = new AtomicInteger(0)
      cache.get("foo", supplier(value1.incrementAndGet()), fakeContext1) mustEqual 1

      val value2 = new AtomicInteger(100)
      cache.get("foo", supplier(value2.incrementAndGet()), fakeContext2) mustEqual 101

      // Recheck to make sure the later calls to get had no effect on the previous ones
      cache.get("foo", supplier(value1.incrementAndGet()), fakeContext1) mustEqual 1
      cache.get("foo", supplier(value2.incrementAndGet()), fakeContext2) mustEqual 101
    }


    "only call valueIfMissing once, no matter how many times we call get on the same key" >> {
      val fakeContext = new Context(new RequestBuilder)
      val cache = createInitializedCache[String, Int](fakeContext)
      val value = new AtomicInteger(0)

      cache.get("foo", supplier(value.incrementAndGet()), fakeContext) mustEqual 1
      cache.get("foo", supplier(value.incrementAndGet()), fakeContext) mustEqual 1
      cache.get("foo", supplier(value.incrementAndGet()), fakeContext) mustEqual 1
    }

    "not call valueIfMissing at all if the key is already in the cache" >> {
      val fakeContext = new Context(new RequestBuilder)
      val cache = createInitializedCache[String, Int](fakeContext)

      val originalValue = new AtomicInteger(0)
      cache.get("foo", supplier(originalValue.incrementAndGet()), fakeContext) mustEqual 1

      val newValue = new AtomicInteger(100)
      cache.get("foo", supplier(newValue.incrementAndGet()), fakeContext) mustEqual 1
    }

    "throw an exception if the cache has been cleaned up for the current request" >> {
      val fakeContext = new Context(new RequestBuilder)
      val cache = createInitializedCache[String, Int](fakeContext)
      val value = new AtomicInteger(0)

      cache.get("foo", supplier(value.incrementAndGet()), fakeContext) mustEqual 1

      cache.cleanupCacheForRequest(fakeContext)

      cache.get("foo", supplier(1), fakeContext) must throwA[CacheNotInitializedException]
    }
  }

  private def createInitializedCache[K, V](context: Context): DedupingCache[K, V] = {
    val cache = new DedupingCache[K, V]
    cache.initCacheForRequest(context)
    cache
  }

  private def supplier[A](value: => A): Supplier[A] = {
    new Supplier[A] {
      override def get(): A = value
    }
  }
}
