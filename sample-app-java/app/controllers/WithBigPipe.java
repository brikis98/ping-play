package controllers;

import com.ybrikman.ping.javaapi.bigpipe.BigPipe;
import com.ybrikman.ping.javaapi.bigpipe.HtmlPagelet;
import com.ybrikman.ping.javaapi.bigpipe.HtmlStreamHelper;
import com.ybrikman.ping.javaapi.bigpipe.Pagelet;
import com.ybrikman.ping.javaapi.bigpipe.PageletRenderOptions;
import data.Response;
import helper.FakeServiceClient;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class WithBigPipe extends Controller {
  private final FakeServiceClient serviceClient;

  @Inject
  public WithBigPipe(FakeServiceClient serviceClient) {
    this.serviceClient = serviceClient;
  }
  
  public Result index() {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    F.Promise<Response> profilePromise = serviceClient.fakeRemoteCallMedium("profile");
    F.Promise<Response> graphPromise = serviceClient.fakeRemoteCallMedium("graph");
    F.Promise<Response> feedPromise = serviceClient.fakeRemoteCallSlow("feed");
    F.Promise<Response> inboxPromise = serviceClient.fakeRemoteCallSlow("inbox");
    F.Promise<Response> adsPromise = serviceClient.fakeRemoteCallFast("ads");
    F.Promise<Response> searchPromise = serviceClient.fakeRemoteCallFast("search");

    // Convert each Promise into a Pagelet which will be rendered as HTML as soon as the data is available.
    Pagelet profile = new HtmlPagelet("profile", profilePromise.map(views.html.helpers.module::apply));
    Pagelet graph = new HtmlPagelet("graph", graphPromise.map(views.html.helpers.module::apply));
    Pagelet feed = new HtmlPagelet("feed", feedPromise.map(views.html.helpers.module::apply));
    Pagelet inbox = new HtmlPagelet("inbox", inboxPromise.map(views.html.helpers.module::apply));
    Pagelet ads = new HtmlPagelet("ads", adsPromise.map(views.html.helpers.module::apply));
    Pagelet search = new HtmlPagelet("search", searchPromise.map(views.html.helpers.module::apply));

    // Use BigPipe to compose the pagelets and render them immediately using a streaming template
    BigPipe bigPipe = new BigPipe(PageletRenderOptions.ClientSide, profile, graph, feed, inbox, ads, search);
    return ok(HtmlStreamHelper.toChunks(views.stream.withBigPipe.apply(bigPipe, profile, graph, feed, inbox, ads, search)));
  }
}
