package com.ybrikman.ping.scalaapi.dedupe

import java.util.concurrent.atomic.AtomicInteger

import org.specs2.mutable.Specification

class TestCache extends Specification {

  "The Cache get method should" >> {
    "return None on an empty cache" >> {
      val cache = new Cache[String, String]()
      cache.get("foo") must beNone
    }

    "return a value that you insert" >> {
      val cache = new Cache[String, String]()
      cache.put("foo", "bar") must beNone
      cache.get("foo") must beSome("bar")
    }

    "return None if you insert one value but get a different one" >> {
      val cache = new Cache[String, String]()
      cache.put("foo", "bar") must beNone
      cache.get("baz") must beNone
    }

    "return a new value when you overwrite an old one" >> {
      val cache = new Cache[String, String]()

      cache.put("foo", "bar") must beNone
      cache.get("foo") must beSome("bar")

      cache.put("foo", "baz") must beSome("bar")
      cache.get("foo") must beSome("baz")
    }
  }

  "The Cache contains method should" >> {
    "return false for any key when the cache is empty" >> {
      val cache = new Cache[String, String]()
      cache.contains("foo") must beFalse
    }

    "return true when you lookup a key that was previously inserted" >> {
      val cache = new Cache[String, String]()
      cache.put("foo", "bar") must beNone
      cache.contains("foo") must beTrue
    }

    "return false when you lookup a different key than the one that was previously inserted" >> {
      val cache = new Cache[String, String]()
      cache.put("foo", "bar") must beNone
      cache.contains("baz") must beFalse
    }
  }

  "Cache getOrElseUpdate method should" >> {
    "return the new value on an empty cache" >> {
      val cache = new Cache[String, String]()
      cache.getOrElseUpdate("foo", "bar") mustEqual "bar"
      cache.get("foo") must beSome("bar")
    }

    "return the old value if the same key had already been inserted and not overwrite the old value" >> {
      val cache = new Cache[String, String]()
      cache.put("foo", "bar") must beNone
      cache.getOrElseUpdate("foo", "baz") mustEqual "bar"
      cache.get("foo") must beSome("bar")
    }

    "evaluate the value exactly once if the key was not already present in the cache" >> {
      val cache = new Cache[String, LazyEvalTestClass]()

      // No value previously present, make sure new value gets evaluated correctly
      val evalCount = new AtomicInteger(0)
      val result = cache.getOrElseUpdate("foo", LazyEvalTestClass(evalCount, failIfEvaluated = false, uniqueId = 5))

      evalCount.get() mustEqual 1
      result.uniqueId mustEqual 5

      // Make sure fetching the value later doesn't cause it to be evaluated again
      val getResult = cache.get("foo")

      getResult.isDefined must beTrue
      getResult.get.uniqueId mustEqual 5
      evalCount.get() mustEqual 1
    }

    "not evaluate the value at all if the key was already present in the cache" >> {
      val cache = new Cache[String, LazyEvalTestClass]()

      val evalCountPrevious = new AtomicInteger(0)
      cache.put("foo", LazyEvalTestClass(evalCountPrevious, failIfEvaluated = false, uniqueId = 5)) must beNone

      evalCountPrevious.get() mustEqual 1

      // Make sure inserting another value at the same key does not result in the new value being evaluated
      val evalCountNew = new AtomicInteger(0)
      val result = cache.getOrElseUpdate("foo", LazyEvalTestClass(evalCountNew, failIfEvaluated = true, uniqueId = 123))

      evalCountNew.get() mustEqual 0
      result.uniqueId mustEqual 5

      // Make sure calling get has no effect on the new value either
      val getResult = cache.get("foo")

      getResult.isDefined must beTrue
      evalCountNew.get() mustEqual 0
      evalCountPrevious.get() mustEqual 1
      getResult.get.uniqueId mustEqual 5
    }
  }

  "Cache putIfAbsent method should" >> {
    "insert the value if the key was not already in the cache" >> {
      val cache = new Cache[String, String]()
      cache.putIfAbsent("foo", "bar") must beNone
      cache.get("foo") must beSome("bar")
    }

    "not overwrite the previous value if it was already in the cache" >> {
      val cache = new Cache[String, String]()
      cache.put("foo", "bar") must beNone
      cache.putIfAbsent("foo", "baz") must beSome("bar")
      cache.get("foo") must beSome("bar")
    }

    "insert the value if the cache contained values for other keys" >> {
      val cache = new Cache[String, String]()
      cache.put("foo", "bar") must beNone
      cache.putIfAbsent("bar", "baz") must beNone
      cache.get("foo") must beSome("bar")
      cache.get("bar") must beSome("baz")
    }
  }

  "Cache remove method should" >> {
    "return None on an empty cache" >> {
      val cache = new Cache[String, String]()
      cache.remove("foo") must beNone
    }

    "remove values that were inserted previously" >> {
      val cache = new Cache[String, String]()

      cache.put("foo", "bar") must beNone
      cache.get("foo") must beSome("bar")

      cache.remove("foo") must beSome("bar")
      cache.get("foo") must beNone
    }

    "only remove the requested keys" >> {
      val cache = new Cache[String, String]()

      cache.put("foo", "bar") must beNone
      cache.get("foo") must beSome("bar")

      cache.put("baz", "blah") must beNone
      cache.get("baz") must beSome("blah")

      cache.remove("foo") must beSome("bar")
      cache.get("foo") must beNone
      cache.get("baz") must beSome("blah")
    }
  }

  "Cache empty method should" >> {
    "leave an empty cache empty" >> {
      val cache = new Cache[String, String]()
      cache.size mustEqual 0
      cache.clear()
      cache.size mustEqual 0
    }

    "remove all values from the cache" >> {
      val cache = new Cache[String, String]()

      cache.put("foo", "bar") must beNone
      cache.get("foo") must beSome("bar")

      cache.put("baz", "blah") must beNone
      cache.get("baz") must beSome("blah")

      cache.clear()

      cache.get("foo") must beNone
      cache.get("baz") must beNone
    }
  }

  "Cache size method should" >> {
    "return 0 for an empty cache" >> {
      val cache = new Cache[String, String]()
      cache.size mustEqual 0
    }

    "return the number of elements inserted into the cache so far" >> {
      val cache = new Cache[String, String]()
      cache.size mustEqual 0

      cache.put("foo", "bar") must beNone
      cache.size mustEqual 1

      cache.put("baz", "blah") must beNone
      cache.size mustEqual 2

      cache.put("baz", "abcdef") must beSome("blah")
      cache.size mustEqual 2
    }
  }

  "The Cache should" >> {
    "behave correctly across many put, get, and remove calls" >> {
      val cache = new Cache[Int, Int]()
      val range = 0 until 1000
      val rangeWrappedInOptions = range.map(Option.apply)

      range.map(i => cache.put(i, i)) must contain(beNone).forall
      range.map(i => cache.get(i)) must containTheSameElementsAs(range.map(Option.apply))
      range.map(i => cache.remove(i)) must containTheSameElementsAs(range.map(Option.apply))
      range.map(i => cache.get(i)) must contain(beNone).forall
    }

    "behave correctly across many getOrElseUpdate calls" >> {
      val cache = new Cache[Int, LazyEvalTestClass]()
      val smallRange = 0 until 500

      // Insert some initial values and make sure they each get evaluated exactly once
      val evalCountsAfterPut = smallRange.map { i =>
        val evalCount = new AtomicInteger(0)
        cache.put(i, LazyEvalTestClass(evalCount, failIfEvaluated = false, uniqueId = i))
        evalCount.get()
      }
      evalCountsAfterPut must contain(1).forall

      val bigRange = 0 until 1000
      val constant = 123456

      // Now use getOrElseUpdate to insert more values; the first half should already be in the cache and therefore
      // skipped (so we expect their evalCount to be 0), while the second half should be new entries that get inserted
      // (so we expect their evalCount to be 1)
      val idsAndEvalCountsAfterGetOrElseUpdate = bigRange.map { i =>
        val evalCount = new AtomicInteger(0)

        val shouldBeEvaluated = !smallRange.contains(i)
        val uniqueId = i + constant
        val result = cache.getOrElseUpdate(i, LazyEvalTestClass(evalCount, !shouldBeEvaluated, uniqueId))

        (result.uniqueId, evalCount.get())
      }
      val expectedIdsAndEvalCountsAfterGetOrElseUpdate = bigRange.map { i =>
        val shouldBeEvaluated = !smallRange.contains(i)
        val uniqueId = i + constant

        val expectedId = if (shouldBeEvaluated) uniqueId else i
        val expectedEvalCount = if (shouldBeEvaluated) 1 else 0

        (expectedId, expectedEvalCount)
      }
      idsAndEvalCountsAfterGetOrElseUpdate must containTheSameElementsAs(expectedIdsAndEvalCountsAfterGetOrElseUpdate)

      // One last sanity check: make sure all the values are in the cache and have a count of 1
      val actualGetResults = bigRange.map(i => cache.get(i).map(_.evalCount.get()))

      actualGetResults must containTheSameElementsAs(bigRange.map(i => Some(1)))
    }
  }

  "Cache getAll method should" >> {
    "return an empty Map for an empty cache" >> {
      val cache = new Cache[String, String]()
      cache.getAll mustEqual Map.empty
    }

    "return a Map with the values in the cache" >> {
      val cache = new Cache[String, String]()

      cache.put("foo", "bar") must beNone
      cache.put("baz", "blah") must beNone

      cache.getAll mustEqual Map("foo"->"bar", "baz" -> "blah")
    }
  }
}

/**
 * Used to test lazy evaluation. This class increments the evalCount whenever the constructor is called. It also fails
 * the test if failIfEvaluated was set to true and the constructor gets called. This can be used to fail a
 * test if some lazy value was evaluated when it should not have been.
 *
 * @param evalCount
 * @param failIfEvaluated
 */
case class LazyEvalTestClass(evalCount: AtomicInteger, failIfEvaluated: Boolean, uniqueId: Int) {
  evalCount.incrementAndGet()
  require(!failIfEvaluated)

  // Need to override the equals method generated by the case class because the AtomicInteger class does not implement
  // equals or hashCode: http://stackoverflow.com/questions/7567502/why-are-two-atomicintegers-never-equal
  override def equals(obj: Any): Boolean = {
    obj match {
      case other: LazyEvalTestClass =>
        evalCount.get() == other.evalCount.get() &&
        failIfEvaluated == other.failIfEvaluated &&
        uniqueId == other.uniqueId
      case _ => false
    }
  }
}

