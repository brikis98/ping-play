package com.ybrikman.ping.scalaapi.dedupe

import Cache._
import play.api.Configuration
import java.util.concurrent.ConcurrentHashMap
import collection.JavaConverters._

/**
 * A Scala wrapper for a Java's ConcurrentHashMap (CHM). Exposes the basic underlying methods of CHM and adds a
 * getOrElseUpdate(key, value) method that lazily evaluates the value parameter only if the key is not already present
 * in the cache.
 *
 * You may be asking, why not just use Scala's ConcurrentMap interface, which already has a getOrElseUpdate method?
 *
 * val cache = new ConcurrentHashMap().asScala
 * cache.getOrElseUpdate("foo", "bar") // BAD idea
 *
 * The answer is because this method is inherited from the MapLike trait, and is NOT a thread safe (atomic) operation!
 *
 * The strategy used in the class below is to wrap all values with a LazyWrapper class that only evaluates the value
 * when explicitly accessed. In the getOrElseUpdate method, we avoid accessing the passed in value unless we know it
 * was the one actually inserted into the cache.
 *
 * For more info, see: http://boundary.com/blog/2011/05/
 *
 * TODO: investigate if boundary's NonBlockingHashMap is as good as they say it is (and what tests they have to prove
 * it).
 *
 * TODO: Java-friendly API
 *
 * @param initialCapacity
 * @param concurrencyLevel
 * @param loadFactor
 * @tparam K
 * @tparam V
 */
class Cache[K, V](initialCapacity: Int, loadFactor: Float, concurrencyLevel: Int) {

  /**
   * Overloaded constructor that creates the cache with initial capacity, concurrency level, and load factor read from
   * config
   *
   * @param config
   * @return
   */
  def this(config: Configuration) = this(
    config.getInt(CONFIG_KEY_INITIAL_CAPACITY).getOrElse(DEFAULT_INITIAL_CAPACITY),
    config.getDouble(CONFIG_KEY_LOAD_FACTOR).map(_.toFloat).getOrElse(DEFAULT_LOAD_FACTOR),
    config.getInt(CONFIG_KEY_CONCURRENCY_LEVEL).getOrElse(DEFAULT_CONCURRENCY_LEVEL)
  )

  /**
   * Empty constructor that uses default values for initial capacity, concurrency level, and load factor
   * @return
   */
  def this() = this(
    DEFAULT_INITIAL_CAPACITY,
    DEFAULT_LOAD_FACTOR,
    DEFAULT_CONCURRENCY_LEVEL
  )

  private val cache = new ConcurrentHashMap[K, LazyWrapper[V]](initialCapacity, loadFactor, concurrencyLevel).asScala

  /**
   * Returns all elements of the cache. Use this method only if you really need all of the elements. Calling it will cause
   * all lazy values to be calculated.
   */
  def getAll: Map[K, V] = {
    val mutable = cache.map { case(key, wrapper) => key -> unwrap(wrapper) }
    mutable.toMap
  }

  /**
   * Returns true if this key is associated with a value in the cache and false otherwise.
   *
   * @param key
   * @return
   */
  def contains(key: K): Boolean = {
    cache.contains(key)
  }

  /**
   * Optionally return the value associated with the given key
   *
   * @param key
   * @return
   */
  def get(key: K): Option[V] = {
    cache.get(key).map(unwrap)
  }

  /**
   * Associate the given key with the given value. Optionally return any value previously associated with the key.
   *
   * @param key
   * @param value
   * @return
   */
  def put(key: K, value: V): Option[V] = {
    cache.put(key, wrap(value)).map(unwrap)
  }

  /**
   * If the given key is already associated with a value, return that value. Otherwise, associate the key with the
   * given value and return None.
   *
   * @param key
   * @param value
   * @return
   */
  def putIfAbsent(key: K, value: V): Option[V] = {
    cache.putIfAbsent(key, wrap(value)).map(unwrap)
  }

  /**
   * Get the value associated with the given key. If no value is already associated, then associate the given value
   * with the key and use it as the return value.
   *
   * Like Scala's ConcurrentMap, the value parameter will be lazily evaluated: that is, it'll only be evaluated if
   * there wasn't already a value associated with the given key. However, unlike Scala's ConcurrentMap, this method is
   * a thread safe (atomic) operation.
   *
   * @param key
   * @param value
   * @return
   */
  def getOrElseUpdate(key: K, value: => V): V = {
    val newWrapper = wrap(value)

    // If there was no previous value, we'll end up calling the .value on newWrapper, which will evaluate it for the
    // first (and last) time
    cache.putIfAbsent(key, newWrapper).getOrElse(newWrapper).value
  }

  /**
   * Remove the key and any associated value from the cache. Optionally return any previously associated value.
   *
   * @param key
   * @return
   */
  def remove(key: K): Option[V] = {
    cache.remove(key).map(unwrap)
  }

  /**
   * Remove all keys and values from the cache
   */
  def clear() {
    cache.clear()
  }

  /**
   * Return how many elements are in the cache
   *
   * @return
   */
  def size: Int = {
    cache.size
  }

  private def wrap[T](value: => T): LazyWrapper[T] = {
    new LazyWrapper[T](value)
  }

  private def unwrap[T](lazyWrapper: LazyWrapper[T]): T = {
    lazyWrapper.value
  }

  override def toString: String = "Cache(%s)".format(cache)

  override def hashCode(): Int = cache.hashCode()

  override def equals(other: Any): Boolean = {
    Option(other) match {
      case Some(otherCache: Cache[_, _]) => cache.equals(otherCache.cache)
      case _ => false
    }
  }
}

/**
 * A wrapper that avoids evaluating the value until explicitly accessed by calling either .value, .equals, .hashCode,
 * or .toString.
 *
 * @param wrapped
 * @tparam T
 */
class LazyWrapper[T](wrapped: => T) {

  // Store in a lazy val to make sure the wrapped value is evaluated at most once
  lazy val value = wrapped

  override def toString: String = "LazyWrapper(%s)".format(value)

  override def hashCode(): Int = value.hashCode()

  override def equals(other: Any): Boolean = {
    Option(other) match {
      case Some(otherLazy: LazyWrapper[_]) => value.equals(otherLazy.value)
      case _ => false
    }
  }
}

object Cache {
  val DEFAULT_INITIAL_CAPACITY = 16
  val DEFAULT_CONCURRENCY_LEVEL = 16
  val DEFAULT_LOAD_FACTOR = 0.75f

  val CONFIG_KEY_INITIAL_CAPACITY = "initialCapacity"
  val CONFIG_KEY_CONCURRENCY_LEVEL = "concurrencyLevel"
  val CONFIG_KEY_LOAD_FACTOR = "loadFactor"
}