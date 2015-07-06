package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ybrikman.ping.javaapi.bigpipe.BigPipe;
import com.ybrikman.ping.javaapi.bigpipe.HtmlPagelet;
import com.ybrikman.ping.javaapi.bigpipe.HtmlStreamHelper;
import com.ybrikman.ping.javaapi.bigpipe.JsonPagelet;
import com.ybrikman.ping.javaapi.bigpipe.Pagelet;
import com.ybrikman.ping.javaapi.bigpipe.PageletRenderOptions;
import data.Response;
import helper.FakeServiceClient;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;

import javax.inject.Inject;

/**
 * A few more BigPipe examples
 */
public class MoreBigPipeExamples extends Controller {
  private final FakeServiceClient serviceClient;

  @Inject
  public MoreBigPipeExamples(FakeServiceClient serviceClient) {
    this.serviceClient = serviceClient;
  }

  /**
   * Renders the exact same page as WithBigPipe#index, but this time with server-side rendering. This will render all
   * pagelets server-side and send them down in-order. The page load time will be longer than with out-of-order
   * client-side rendering (albeit still faster than not using BigPipe at all), but the advantage is that server-side
   * rendering does not depend on JavaScript, which is important for certain use cases (e.g. older browsers, search
   * engine crawlers, SEO).
   *
   * @return
   */
  public Result serverSideRendering() {
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

    // Use BigPipe to compose the pagelets and render them immediately using a streaming template. Note that we're using
    // ServerSide rendering in this case.
    BigPipe bigPipe = new BigPipe(PageletRenderOptions.ServerSide, profile, graph, feed, inbox, ads, search);
    return ok(HtmlStreamHelper.toChunks(views.stream.withBigPipe.apply(bigPipe, profile, graph, feed, inbox, ads, search)));
  }

  /**
   * Instead of rendering each pagelet server-side with Play's templating, you can send back JSON and render each
   * pagelet with a client-side templating library such as mustache.js
   *
   * @return
   */
  public Result clientSideTemplating() {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    F.Promise<JsonNode> profilePromise = serviceClient.fakeRemoteCallJsonMedium("profile");
    F.Promise<JsonNode> graphPromise = serviceClient.fakeRemoteCallJsonMedium("graph");
    F.Promise<JsonNode> feedPromise = serviceClient.fakeRemoteCallJsonSlow("feed");
    F.Promise<JsonNode> inboxPromise = serviceClient.fakeRemoteCallJsonSlow("inbox");
    F.Promise<JsonNode> adsPromise = serviceClient.fakeRemoteCallJsonFast("ads");
    F.Promise<JsonNode> searchPromise = serviceClient.fakeRemoteCallJsonFast("search");

    Pagelet profile = new JsonPagelet("profile", profilePromise);
    Pagelet graph = new JsonPagelet("graph", graphPromise);
    Pagelet feed = new JsonPagelet("feed", feedPromise);
    Pagelet inbox = new JsonPagelet("inbox", inboxPromise);
    Pagelet ads = new JsonPagelet("ads", adsPromise);
    Pagelet search = new JsonPagelet("search", searchPromise);

    // Use BigPipe to compose the pagelets and render them immediately using a streaming template
    BigPipe bigPipe = new BigPipe(PageletRenderOptions.ClientSide, profile, graph, feed, inbox, ads, search);
    return ok(HtmlStreamHelper.toChunks(views.stream.clientSideTemplating.apply(bigPipe, profile, graph, feed, inbox, ads, search)));
  }

  /**
   * Shows an example of how to handle an error that occurs part way through streaming a response to the browser. Since
   * you've already sent back the headers with a 200 OK, it's too late to send back a 500 error page, so instead, you
   * have to inject JavaScript into the stream that will show an appropriate error page.
   *
   * @return
   */
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
    Pagelet profile = new HtmlPagelet("profile", render(profilePromise));
    Pagelet graph = new HtmlPagelet("graph", render(graphPromise));
    Pagelet feed = new HtmlPagelet("feed", render(feedPromise));
    Pagelet inbox = new HtmlPagelet("inbox", render(inboxPromise));
    Pagelet ads = new HtmlPagelet("ads", render(adsPromise));
    Pagelet search = new HtmlPagelet("search", render(searchPromise));

    // Use BigPipe to compose the pagelets and render them immediately using a streaming template
    BigPipe bigPipe = new BigPipe(PageletRenderOptions.ClientSide, profile, graph, feed, inbox, ads, search);
    return ok(HtmlStreamHelper.toChunks(views.stream.withBigPipe.apply(bigPipe, profile, graph, feed, inbox, ads, search)));
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
