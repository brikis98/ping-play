package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ybrikman.ping.javaapi.promise.PromiseHelper;
import data.ServiceClient;
import data.UrlAndId;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * This controller shows an example of how remote service calls can be transparently de-duped using the DedupingCache
 * to ensure that we only make one remote call for each unique URL.
 */
public class Deduping extends Controller {
  private final ServiceClient serviceClient;

  @Inject
  public Deduping(ServiceClient serviceClient) {
    this.serviceClient = serviceClient;
  }
  
  public F.Promise<Result> index() {
    // Call an endpoint on this same Play app that returns the request id, which should be unique for every incoming
    // request
    String baseUrl = "http://" + request().host();
    String url1 = baseUrl + "/mock/requestId";
    String url2 = baseUrl + "/mock/requestId?foo=bar";

    // Thanks to the DedupingCache in the ServiceClient, all 3 calls to url1 will result in only a single remote call
    // and the call to url2 will result in a separate call
    F.Promise<WSResponse> promise1 = serviceClient.remoteCall(url1);
    F.Promise<WSResponse> promise2 = serviceClient.remoteCall(url1);
    F.Promise<WSResponse> promise3 = serviceClient.remoteCall(url1);
    F.Promise<WSResponse> promise4 = serviceClient.remoteCall(url2);

    return PromiseHelper.sequence(promise1, promise2, promise3, promise4).map((result1, result2, result3, result4) -> {
      // We should expect to see the same request id for the first 3 requests (since deduping should ensure only one
      // request is actually made) and a different id for the fourth one

      UrlAndId urlAndId1 = new UrlAndId(url1, result1.getBody());
      UrlAndId urlAndId2 = new UrlAndId(url1, result2.getBody());
      UrlAndId urlAndId3 = new UrlAndId(url1, result3.getBody());
      UrlAndId urlAndId4 = new UrlAndId(url2, result4.getBody());

      return ok(views.html.dedupe.apply(urlAndId1, urlAndId2, urlAndId3, urlAndId4));
    });
  }
}
