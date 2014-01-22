package data;

import play.libs.F;
import play.libs.WS;

public class ServiceClientJ
{
  public static F.Promise<String> makeServiceCall(String serviceName)
  {
    return WS.url("http://localhost:9000/mock/" + serviceName).get().map(new F.Function<WS.Response, String>()
    {
      @Override
      public String apply(WS.Response response) throws Throwable
      {
        return response.getBody();
      }
    });
  }
}
