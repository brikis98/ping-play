package com.ybrikman.ping.scalaapi.dedupe

import play.api.mvc.RequestHeader

/**
 * A cache you can use to de-dupe expensive calculations to ensure that the same calculation happens at most once while
 * processing any incoming request. For example, imagine you get an incoming request to /foo, and to render this page,
 * you have to make a dozen calls to remote services (e.g. a profile service, a search service, etc) using REST over
 * HTTP. You can use the DedupingCache to ensure you don't make the same exact call more than once (e.g. fetch the
 * exact same profile multiple times) by running all the calls through this client. You would use the URL of the REST
 * call as the key and the Future returned from the call as the value:
 *
 * val remoteUrl = "http://example.com/some/remote/service"
 * val future: Future[Response] = dedupingCache.get(remoteUrl, WS.url(remoteUrl).get())
 *
 * While processing any incoming request, using the code above ensures that you will not make multiple calls to the
 * exact same remoteUrl; any duplicates will just return a Future object that is already in the cache.
 *
 * You should only use the DedupingCache for data that is safe to cache. For example, HTTP GET calls are usually safe
 * to cache since they should be idempotent, but HTTP POST calls are not safe to cache. Also, you need to add the
 * CacheFilter to your filter chain so that it can clean up the cache after you're done processing an incoming request.
 * Otherwise, you'll have a memory leak.
 *
 * @tparam K The type to use for keys. This type must define an equals and hashCode method. For example, if you're
 *           making REST over HTTP calls, the HTTP URL is a good key.
 * @tparam V The type of value that will be returned. For example, if you're making REST over HTTP calls using Play's
 *           WS library, a Future[Response] might be a good type for the value.
 */
class DedupingCache[K, V] {
  private val cache = new Cache[Long, Cache[K, V]]()

  /**
   * Get the value for key K from the cache. If the value is not already in teh cache, use the valueIfMissing function
   * to calculate a value, store it in the cache, and return that value.
   *
   * @param key
   * @param valueIfMissing
   * @param playRequest
   * @return
   */
  def get(key: K, valueIfMissing: => V)(implicit playRequest: RequestHeader): V = {
    getCacheForPlayRequest(playRequest).getOrElseUpdate(key, valueIfMissing)
  }

  /**
   * Initialize the cache for the given incoming request. Should only be used by the CacheFilter.
   *
   * @param playRequest
   */
  def initCacheForRequest(playRequest: RequestHeader): Unit = {
    cache.put(playRequest.id, new Cache[K, V])
  }

  /**
   * Cleanup the cache after you're completely done processing an incoming request. This is necessary to prevent memory
   * leaks. Should only be used by the CacheFilter.
   *
   * @param playRequest
   */
  def cleanupCacheForRequest(playRequest: RequestHeader): Unit = {
    cache.remove(playRequest.id)
  }

  private def getCacheForPlayRequest(playRequest: RequestHeader): Cache[K, V] = {
    cache.get(playRequest.id).getOrElse(throw new CacheNotInitializedException(
      s"No cache found for request with id ${playRequest.id}. " +
      s"Did you add ${classOf[CacheFilter[_, _]].getName} to your filter chain?"))
  }
}
