package controllers;

import data.ServiceClientJ;
import play.api.templates.Html;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Function1;
import ui.HtmlStream;
import ui.Pagelet;

import java.util.Arrays;

public class WvypStreamJava extends Controller
{
  public static Result index()
  {
    F.Promise<String> wvypPromise = ServiceClientJ.makeServiceCall("wvyp");
    F.Promise<String> searchPromise = ServiceClientJ.makeServiceCall("search");

    F.Promise<Html> wvypHtmlPromise = render(wvypPromise, views.html.wvyp.wvypCount.f());
    F.Promise<Html> searchHtmlPromise = render(searchPromise, views.html.wvyp.searchCount.f());

    HtmlStream wvypStream = Pagelet.renderStream(wvypHtmlPromise, "wvypCount");
    HtmlStream searchStream = Pagelet.renderStream(searchHtmlPromise, "searchCount");

    HtmlStream body = HtmlStream.interleave(Arrays.asList(wvypStream, searchStream));

    return ok(HtmlStream.toChunks(views.stream.wvyp.wvyp.apply(body)));
  }

  // Render the contents of the given Promise as Html using the given template. Templates are functions in Play, and
  // passing them around in Java is a bit clunky
  private static F.Promise<Html> render(F.Promise<String> promise, final Function1<Object, Html> template)
  {
    return promise.map(new F.Function<String, Html>()
    {
      @Override
      public Html apply(String s) throws Throwable
      {
        return template.apply(Integer.parseInt(s));
      }
    });
  }
}
