package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ybrikman.ping.javaapi.bigpipe.HtmlStreamHelper;
import com.ybrikman.ping.javaapi.bigpipe.Pagelet;
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStream;
import helper.FakeServiceClient;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

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
}
