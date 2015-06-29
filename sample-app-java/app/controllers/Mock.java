package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * This controller is used as a mock remote endpoint in other tests. This allows you to make actual remote calls, but
 * still completely control the response you get back.
 */
public class Mock extends Controller {

  /**
   * An endpoint that returns the request id of the incoming request
   *
   * @return
   */
  public Result requestId() {
    return ok(ctx().id().toString());
  }
}
