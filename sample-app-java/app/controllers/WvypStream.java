package controllers;

import com.ybrikman.ping.javaapi.bigpipe.HtmlStreamHelper;
import com.ybrikman.ping.javaapi.bigpipe.Pagelet;
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStream;
import data.ServiceClient;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;

import javax.inject.Inject;

public class WvypStream extends Controller {
  private final ServiceClient serviceClient;

  @Inject
  public WvypStream(ServiceClient serviceClient) {
    this.serviceClient = serviceClient;
  }

  public Result index() {
    F.Promise<String> wvypPromise = serviceClient.makeServiceCall("wvyp");
    F.Promise<String> searchPromise = serviceClient.makeServiceCall("search");

    F.Promise<Html> wvypHtmlPromise = wvypPromise.map(str -> views.html.wvyp.wvypCount.apply(Integer.parseInt(str)));
    F.Promise<Html> searchHtmlPromise = searchPromise.map(str -> views.html.wvyp.searchCount.apply(Integer.parseInt(str)));

    Pagelet wvypPagelet = Pagelet.fromHtmlPromise(wvypHtmlPromise, "wvypCount");
    Pagelet searchPagelet = Pagelet.fromHtmlPromise(searchHtmlPromise, "searchCount");

    HtmlStream body = HtmlStreamHelper.fromInterleavedPagelets(wvypPagelet, searchPagelet);

    return ok(HtmlStreamHelper.toChunks(views.stream.wvyp.wvyp.apply(body)));
  }
}