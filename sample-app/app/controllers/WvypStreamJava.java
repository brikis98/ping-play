package controllers;

import com.ybrikman.ping.javaapi.bigpipe.HtmlStreamHelper;
import com.ybrikman.ping.javaapi.bigpipe.Pagelet;
import data.ServiceClientJ;
import play.twirl.api.Html;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStream;

public class WvypStreamJava extends Controller
{
  private final ServiceClientJ serviceClient;

  public WvypStreamJava(ServiceClientJ serviceClient) {
    this.serviceClient = serviceClient;
  }

  public Result index()
  {
    F.Promise<String> wvypPromise = serviceClient.makeServiceCall("wvyp", request());
    F.Promise<String> searchPromise = serviceClient.makeServiceCall("search", request());

    F.Promise<Html> wvypHtmlPromise = wvypPromise.map(str -> views.html.wvyp.wvypCount.apply(Integer.parseInt(str)));
    F.Promise<Html> searchHtmlPromise = searchPromise.map(str -> views.html.wvyp.searchCount.apply(Integer.parseInt(str)));

    Pagelet wvypPagelet = Pagelet.fromHtmlPromise(wvypHtmlPromise, "wvypCount");
    Pagelet searchPagelet = Pagelet.fromHtmlPromise(searchHtmlPromise, "searchCount");

    HtmlStream body = HtmlStreamHelper.fromInterleavedPagelets(wvypPagelet, searchPagelet);

    return ok(HtmlStreamHelper.toChunks(views.stream.wvyp.wvyp.apply(body)));
  }
}
