package controllers;

import data.ServiceClient;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class Wvyu extends Controller {
  private final ServiceClient serviceClient;

  @Inject
  public Wvyu(ServiceClient serviceClient) {
    this.serviceClient = serviceClient;
  }

  public F.Promise<Result> index(Boolean embed) {
    F.Promise<String> likesPromise = serviceClient.makeServiceCall("likes");
    F.Promise<String> commentsPromise = serviceClient.makeServiceCall("comments");

    return likesPromise.flatMap(likes -> commentsPromise.map(comments -> {
      int likesCount = Integer.parseInt(likes);
      int commentsCount = Integer.parseInt(comments);

      if (embed) {
        return ok(views.html.wvyu.wvyuBody.apply(likesCount, commentsCount));
      } else {
        return ok(views.html.wvyu.wvyu.apply(likesCount, commentsCount));
      }
    }));
  }
}