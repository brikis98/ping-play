package com.ybrikman.ping.javaapi.dedupe;

import com.ybrikman.ping.scalaapi.bigpipe.JavaAdapter;
import com.ybrikman.ping.scalaapi.dedupe.Cache;
import com.ybrikman.ping.scalaapi.dedupe.CacheNotInitializedException;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.function.Supplier;

/**
 * A cache you can use to de-dupe expensive calculations to ensure that the same calculation happens at most once while
 * processing any incoming request. For example, imagine you get an incoming request to /foo, and to render this page,
 * you have to make a dozen calls to remote services (e.g. a profile service, a search service, etc) using REST over
 * HTTP. You can use the DedupingCache to ensure you don't make the same exact call more than once (e.g. fetch the
 * exact same profile multiple times) by running all the calls through this client. You would use the URL of the REST
 * call as the key and the Future returned from the call as the value:
 *
 * String remoteUrl = "http://example.com/some/remote/service";
 * Promise<Response> promise = dedupingCache.get(remoteUrl, WS.url(remoteUrl).get());
 *
 * While processing any incoming request, using the code above ensures that you will not make multiple calls to the
 * exact same remoteUrl; any duplicates will just return a Promise object that is already in the cache.
 *
 * You should only use the DedupingCache for data that is safe to cache. For example, HTTP GET calls are usually safe
 * to cache since they should be idempotent, but HTTP POST calls are not safe to cache. Also, you need to add the
 * CacheFilter to your filter chain so that it can clean up the cache after you're done processing an incoming request.
 * Otherwise, you'll have a memory leak.
 *
 * @param <K> The type to use for keys. This type must define an equals and hashCode method. For example, if you're
 *            making REST over HTTP calls, the HTTP URL is a good key.
 * @param <V> The type of value that will be returned. For example, if you're making REST over HTTP calls using Play's
 *            WS library, a Promise<Response> might be a good type for the value.
 */
@Singleton
public class DedupingCache<K, V> {
  private final Cache<Long, Cache<K, V>> cache;

  public DedupingCache() {
    cache = new Cache<>();
  }

  /**
   * Get the value for key K from the cache. If the value is not already in teh cache, use the valueIfMissing function
   * to calculate a value, store it in the cache, and return that value.
   *
   * @param key
   * @param valueIfMissing
   * @return
   */
  public V get(K key, Supplier<V> valueIfMissing) {
    return get(key, valueIfMissing, Http.Context.current());
  }

  /**
   * Get the value for key K from the cache. If the value is not already in teh cache, use the valueIfMissing function
   * to calculate a value, store it in the cache, and return that value. This version of the method allows you to
   * explicitly specify the HTTP context.
   *
   * @param key
   * @param valueIfMissing
   * @param context
   * @return
   */
  public V get(K key, Supplier<V> valueIfMissing, Http.Context context) {
    return getCacheForPlayRequest(context).getOrElseUpdate(key, JavaAdapter.javaSupplierToScalaFunction(valueIfMissing));
  }

  /**
   * Initialize the cache for the given incoming request. Should only be used by the CacheFilter.
   *
   * @param context
   */
  public void initCacheForRequest(Http.Context context) {
    cache.put(context.id(), new Cache<>());
  }

  /**
   * Cleanup the cache after you're completely done processing an incoming request. This is necessary to prevent memory
   * leaks. Should only be used by the CacheFilter.
   *
   * @param context
   */
  public void cleanupCacheForRequest(Http.Context context) {
    cache.remove(context.id());
  }

  private Cache<K, V> getCacheForPlayRequest(Http.Context context) {
    return cache.get(context.id()).getOrElse(JavaAdapter.javaSupplierToScalaFunction(() -> {
      throw new CacheNotInitializedException(
          "No cache found for request with id " + context.id() + " Did you add " +
              CacheFilter.class.getName() + " to your filter chain?");
    }));
  }
}
