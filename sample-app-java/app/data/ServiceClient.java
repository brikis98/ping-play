package data;

import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import javax.inject.Inject;

public class ServiceClient {
  private final WSClient wsClient;

  @Inject
  public ServiceClient(WSClient wsClient) {
    this.wsClient = wsClient;
  }

  public F.Promise<String> makeServiceCall(String serviceName) {
    String url = "http://localhost:" + RequestUtil.port(Http.Context.current().request()) + "/mock/" + serviceName;
    return wsClient.url(url).get().map(WSResponse::getBody);
  }
}
