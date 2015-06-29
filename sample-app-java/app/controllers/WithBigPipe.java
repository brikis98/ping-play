package controllers;

import com.ybrikman.ping.javaapi.bigpipe.HtmlStreamHelper;
import com.ybrikman.ping.javaapi.bigpipe.Pagelet;
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStream;
import data.Response;
import helper.ServiceClient;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class WithBigPipe extends Controller {
  private final ServiceClient serviceClient;

  @Inject
  public WithBigPipe(ServiceClient serviceClient) {
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

    Pagelet profile = Pagelet.fromHtmlPromise(profilePromise.map(views.html.helpers.module::apply), "profile");
    Pagelet graph = Pagelet.fromHtmlPromise(graphPromise.map(views.html.helpers.module::apply), "graph");
    Pagelet feed = Pagelet.fromHtmlPromise(feedPromise.map(views.html.helpers.module::apply), "feed");
    Pagelet inbox = Pagelet.fromHtmlPromise(inboxPromise.map(views.html.helpers.module::apply), "inbox");
    Pagelet ads = Pagelet.fromHtmlPromise(adsPromise.map(views.html.helpers.module::apply), "ads");
    Pagelet search = Pagelet.fromHtmlPromise(searchPromise.map(views.html.helpers.module::apply), "search");

    // Compose all the pagelets into an HtmlStream
    HtmlStream body = HtmlStreamHelper.fromInterleavedPagelets(profile, graph, feed, inbox, ads, search);

    // Render the streaming template immediately
    return ok(HtmlStreamHelper.toChunks(views.stream.withBigPipe.apply(body)));
  }
}
