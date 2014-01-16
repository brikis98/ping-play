package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.iteratee.Enumerator

object Enum extends Controller {

  def index = Action {
    val helloEnumerator = Enumerator("hello ")
    val goodbyeEnumerator = Enumerator("goodbye\n\n")

    val helloGoodbyeEnumerator = helloEnumerator.andThen(goodbyeEnumerator)

    Ok.feed(helloGoodbyeEnumerator)
  }
}
