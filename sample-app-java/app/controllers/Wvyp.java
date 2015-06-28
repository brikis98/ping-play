package controllers;

import data.ServiceClient;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class Wvyp extends Controller {

  private final ServiceClient serviceClient;

  @Inject
  public Wvyp(ServiceClient serviceClient) {
    this.serviceClient = serviceClient;
  }

  public F.Promise<Result> index(Boolean embed) {
    F.Promise<String> wvypPromise = serviceClient.makeServiceCall("wvyp");
    F.Promise<String> searchPromise = serviceClient.makeServiceCall("search");

    return wvypPromise.flatMap(wvyp -> searchPromise.map(search -> {
      int wvypCount = Integer.parseInt(wvyp);
      int searchCount = Integer.parseInt(search);

      if (embed) {
        return ok(views.html.wvyp.wvypBody.apply(wvypCount, searchCount));
      } else {
        return ok(views.html.wvyp.wvyp.apply(wvypCount, searchCount));
      }
    }));
  }
}
