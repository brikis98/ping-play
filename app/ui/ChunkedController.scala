package ui

import play.api.mvc.{Action, Controller}
import ui.HtmlStreamImplicits._

/**
 * Created by ahmed on 3/23/2015.
 */
abstract class ChunkedController extends Controller {

  def index = Action {
    val pipe = HtmlStream.interleave(stream())
    Ok.chunked(views.stream.page(holder(), pipe))
  }

  def stream(): HtmlStream

  def holder(): HtmlStream


}
