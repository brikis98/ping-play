package data;

import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http.Request;

public class ServiceClientJ {
  private final WSClient wsClient;

  public ServiceClientJ(WSClient wsClient) {
    this.wsClient = wsClient;
  }

  public F.Promise<String> makeServiceCall(String serviceName, Request request) {
    String url = "http://localhost:" + RequestUtil.port(request) + "/mock/" + serviceName;
    return wsClient.url(url).get().map(WSResponse::getBody);
  }
}
