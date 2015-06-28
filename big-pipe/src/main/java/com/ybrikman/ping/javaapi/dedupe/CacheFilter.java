package com.ybrikman.ping.javaapi.dedupe;

import com.ybrikman.ping.scalaapi.dedupe.BeforeAndAfterFilter;
import com.ybrikman.ping.scalaapi.dedupe.JavaFunctionHelper;
import play.libs.HttpExecution;
import play.mvc.Http;
import scala.concurrent.ExecutionContext;

public class CacheFilter<K, V> extends BeforeAndAfterFilter {
  public CacheFilter(DedupingCache<K, V> cache) {
    this(cache, HttpExecution.defaultContext());
  }

  public CacheFilter(DedupingCache<K, V> cache, ExecutionContext executionContext) {
    this(cache, executionContext, Http.Context.current());
  }

  public CacheFilter(DedupingCache<K, V> cache, ExecutionContext executionContext, Http.Context context) {
    super(
        JavaFunctionHelper.toScalaFunction(rh -> cache.initCacheForRequest(context)),
        JavaFunctionHelper.toScalaFunction(rh -> cache.cleanupCacheForRequest(context)),
        executionContext);
  }
}
