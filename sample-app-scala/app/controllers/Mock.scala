package controllers

import play.api.mvc.{Controller, Action}

/**
 * This controller is used as a mock remote endpoint in other tests. This allows you to make actual remote calls, but
 * still completely control the response you get back.
 */
class Mock extends Controller {

  /**
   * An endpoint that returns the request id of the incoming request
   *
   * @return
   */
  def requestId = Action { request =>
    Ok(request.id.toString)
  }
}
