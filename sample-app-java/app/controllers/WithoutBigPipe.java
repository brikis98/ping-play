package controllers;

import com.ybrikman.ping.javaapi.promise.PromiseHelper;
import data.Response;
import helper.FakeServiceClient;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class WithoutBigPipe extends Controller {

  private final FakeServiceClient serviceClient;

  @Inject
  public WithoutBigPipe(FakeServiceClient serviceClient) {
    this.serviceClient = serviceClient;
  }

  public F.Promise<Result> index() {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    F.Promise<Response> profilePromise = serviceClient.fakeRemoteCallMedium("profile");
    F.Promise<Response> graphPromise = serviceClient.fakeRemoteCallMedium("graph");
    F.Promise<Response> feedPromise = serviceClient.fakeRemoteCallSlow("feed");
    F.Promise<Response> inboxPromise = serviceClient.fakeRemoteCallSlow("inbox");
    F.Promise<Response> adsPromise = serviceClient.fakeRemoteCallFast("ads");
    F.Promise<Response> searchPromise = serviceClient.fakeRemoteCallFast("search");

    return PromiseHelper
        .sequence(profilePromise, graphPromise, feedPromise, inboxPromise, adsPromise, searchPromise)
        .map((profile, graph, feed, inbox, ads, search) ->
          ok(views.html.withoutBigPipe.apply(profile, graph, feed, inbox, ads, search))
        );
  }
}
