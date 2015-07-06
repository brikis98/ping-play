package com.ybrikman.ping.javaapi.dedupe;

import com.ybrikman.ping.scalaapi.bigpipe.JavaAdapter;
import com.ybrikman.ping.scalaapi.dedupe.BeforeAndAfterFilter;
import play.libs.HttpExecution;
import play.mvc.Http;
import scala.concurrent.ExecutionContext;

/**
 * Any time you use the DedupingCache, you must add this CacheFilter to your filter chain. This filter will initialize
 * the cache for each incoming request and cleanup the cache after you're done processing the request. To avoid memory
 * leaks, you want to be sure this filter runs on every single request, so it's a good idea to make it the very first
 * one in the filter chain, so no other filter can bypass it.
 *
 * @param <K>
 * @param <V>
 */
public class CacheFilter<K, V> extends BeforeAndAfterFilter {
  public CacheFilter(DedupingCache<K, V> cache) {
    this(cache, HttpExecution.defaultContext());
  }

  public CacheFilter(DedupingCache<K, V> cache, ExecutionContext executionContext) {
    super(
        JavaAdapter.javaConsumerToScalaFunction(rh -> cache.initCacheForRequest(contextFromRequestHeader(rh))),
        JavaAdapter.javaConsumerToScalaFunction(rh -> cache.cleanupCacheForRequest(contextFromRequestHeader(rh))),
        executionContext);
  }

  private static Http.Context contextFromRequestHeader(play.api.mvc.RequestHeader rh) {
    return new Http.Context(new Http.RequestImpl(rh));
  }
}
