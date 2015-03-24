package controllers

import play.api.mvc.{Action, Controller}
import ui.HtmlStream
import ui.HtmlStreamImplicits._

/**
 * An example standalone module that represents the "Who's Viewed Your Profile" module from the LinkedIn homepage.
 * It's identical to the ProfileViewsController controller, except that the data is streamed to the browser as soon as it's available in
 * small "pagelets", similar to Facebook's BigPipe.
 */
object CompositeStreamController extends Controller {

  def index = Action {

    val profileStream = ProfileViewsStreamController.stream
    val updateStream = UpdateViewsStreamController.stream

    val pipe = HtmlStream.interleave(profileStream, updateStream)

    Ok.chunked(views.stream.index(UpdateViewsStreamController.holder(), ProfileViewsStreamController.holder(), pipe))
  }
}
