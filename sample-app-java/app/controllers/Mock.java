package controllers;

import akka.actor.ActorSystem;
import data.FutureUtil;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class Mock extends Controller {

  private final FutureUtil futureUtil;

  @Inject
  public Mock(ActorSystem actorSystem) {
    futureUtil = new FutureUtil(actorSystem);
  }

  public static final String DEFAULT_WVYP_RESPONSE = "56";
  public static final String DEFAULT_SEARCH_RESPONSE = "10";
  public static final String DEFAULT_LIKES_RESPONSE = "150";
  public static final String DEFAULT_COMMENTS_RESPONSE = "14";

  public static final long FAST_RESPONSE_TIME = 10;
  public static final long SLOW_RESPONSE_TIME = 3000;
  
  public F.Promise<Result> mock(String serviceName) {
    switch(serviceName) {
      case "wvyp": return respond(DEFAULT_WVYP_RESPONSE, FAST_RESPONSE_TIME);
      case "search": return respond(DEFAULT_SEARCH_RESPONSE, SLOW_RESPONSE_TIME);
      case "likes": return respond(DEFAULT_LIKES_RESPONSE, FAST_RESPONSE_TIME);
      case "comments": return respond(DEFAULT_COMMENTS_RESPONSE, FAST_RESPONSE_TIME);
      default:return F.Promise.pure(badRequest("Unsupported service name: " + serviceName));
    }
  }

  private F.Promise<Result> respond(String data, Long delay) {
    return futureUtil.timeout(() -> ok(data), delay, TimeUnit.MILLISECONDS);
  }
}
