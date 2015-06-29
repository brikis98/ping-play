package data;

import com.ybrikman.ping.javaapi.dedupe.DedupingCache;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;

public class ServiceClient {
  private final WSClient ws;
  private final DedupingCache<String, F.Promise<WSResponse>> cache;

  @Inject
  public ServiceClient(WSClient ws, DedupingCache<String, F.Promise<WSResponse>> cache) {
    this.ws = ws;
    this.cache = cache;
  }

  public F.Promise<WSResponse> remoteCall(String url) {
    return cache.get(url, () -> ws.url(url).get());
  }
}
