package data;

import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Http.Request;

public class ServiceClientJ
{
  public static F.Promise<String> makeServiceCall(String serviceName, Request request)
  {
    String url = "http://localhost:" + RequestUtil.port(request) + "/mock/" + serviceName;
    return WS.url(url).get().map(new F.Function<WSResponse, String>()
    {
      @Override
      public String apply(WSResponse response) throws Throwable
      {
        return response.getBody();
      }
    });
  }
}
