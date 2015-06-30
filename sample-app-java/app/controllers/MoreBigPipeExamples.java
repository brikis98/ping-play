package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ybrikman.ping.javaapi.bigpipe.HtmlStreamHelper;
import com.ybrikman.ping.javaapi.bigpipe.Pagelet;
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStream;
import data.Response;
import helper.FakeServiceClient;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;

import javax.inject.Inject;

public class MoreBigPipeExamples extends Controller {
  private final FakeServiceClient serviceClient;

  @Inject
  public MoreBigPipeExamples(FakeServiceClient serviceClient) {
    this.serviceClient = serviceClient;
  }

  public Result clientSideTemplating() {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    F.Promise<JsonNode> profilePromise = serviceClient.fakeRemoteCallJsonMedium("profile");
    F.Promise<JsonNode> graphPromise = serviceClient.fakeRemoteCallJsonMedium("graph");
    F.Promise<JsonNode> feedPromise = serviceClient.fakeRemoteCallJsonSlow("feed");
    F.Promise<JsonNode> inboxPromise = serviceClient.fakeRemoteCallJsonSlow("inbox");
    F.Promise<JsonNode> adsPromise = serviceClient.fakeRemoteCallJsonFast("ads");
    F.Promise<JsonNode> searchPromise = serviceClient.fakeRemoteCallJsonFast("search");

    Pagelet profile = Pagelet.fromJsonPromise(profilePromise, "profile");
    Pagelet graph = Pagelet.fromJsonPromise(graphPromise, "graph");
    Pagelet feed = Pagelet.fromJsonPromise(feedPromise, "feed");
    Pagelet inbox = Pagelet.fromJsonPromise(inboxPromise, "inbox");
    Pagelet ads = Pagelet.fromJsonPromise(adsPromise, "ads");
    Pagelet search = Pagelet.fromJsonPromise(searchPromise, "search");

    // Compose all the pagelets into an HtmlStream
    HtmlStream body = HtmlStreamHelper.fromInterleavedPagelets(profile, graph, feed, inbox, ads, search);

    // Render the streaming template immediately
    return ok(HtmlStreamHelper.toChunks(views.stream.clientSideTemplating.apply(body)));
  }

  public Result errorHandling() {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    F.Promise<Response> profilePromise = serviceClient.fakeRemoteCallMedium("profile");
    F.Promise<Response> graphPromise = serviceClient.fakeRemoteCallMedium("graph");
    F.Promise<Response> feedPromise = serviceClient.fakeRemoteCallErrorSlow("feed");
    F.Promise<Response> inboxPromise = serviceClient.fakeRemoteCallSlow("inbox");
    F.Promise<Response> adsPromise = serviceClient.fakeRemoteCallFast("ads");
    F.Promise<Response> searchPromise = serviceClient.fakeRemoteCallFast("search");

    // Convert each Promise into a Pagelet which will be rendered as HTML as soon as the data is available. Note that
    // the render method used here will also handle the case where the Future completes with an error by rendering an
    // error message.
    Pagelet profile = Pagelet.fromHtmlPromise(render(profilePromise), "profile");
    Pagelet graph = Pagelet.fromHtmlPromise(render(graphPromise), "graph");
    Pagelet feed = Pagelet.fromHtmlPromise(render(feedPromise), "feed");
    Pagelet inbox = Pagelet.fromHtmlPromise(render(inboxPromise), "inbox");
    Pagelet ads = Pagelet.fromHtmlPromise(render(adsPromise), "ads");
    Pagelet search = Pagelet.fromHtmlPromise(render(searchPromise), "search");

    // Compose all the pagelets into an HtmlStream
    HtmlStream body = HtmlStreamHelper.fromInterleavedPagelets(profile, graph, feed, inbox, ads, search);

    // Render the streaming template immediately
    return ok(HtmlStreamHelper.toChunks(views.stream.withBigPipe.apply(body)));
  }

  /**
   * When the given Future redeems, render it with the module template. If the Future fails, render it with the
   * error template.
   *
   * @param dataPromise
   * @return
   */
  private F.Promise<Html> render(F.Promise<Response> dataPromise) {
    return dataPromise.map(views.html.helpers.module::apply).recover(views.html.helpers.error::apply);
  }
}
