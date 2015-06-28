package com.ybrikman.ping.scalaapi.dedupe

import scala.concurrent.ExecutionContext

/**
 * Any time you use the DedupingCache, you must add this CacheFilter to your filter chain. This filter will initialize
 * the cache for each incoming request and cleanup the cache after you're done processing the request. To avoid memory
 * leaks, you want to be sure this filter runs on every single request, so it's a good idea to make it the very first
 * one in the filter chain, so no other filter can bypass it.
 *
 * @param dedupingCache
 * @tparam K
 * @tparam V
 */
class CacheFilter[K, V](dedupingCache: DedupingCache[K, V])(implicit ec: ExecutionContext) extends BeforeAndAfterFilter(
  before = rh => dedupingCache.initCacheForRequest(rh),
  after = rh => dedupingCache.cleanupCacheForRequest(rh))

