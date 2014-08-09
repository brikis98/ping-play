package data;

import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

public class ServiceClientJ
{
  public static F.Promise<String> makeServiceCall(String serviceName)
  {
    return WS.url("http://localhost:9000/mock/" + serviceName).get().map(new F.Function<WSResponse, String>()
    {
      @Override
      public String apply(WSResponse response) throws Throwable
      {
        return response.getBody();
      }
    });
  }
}
