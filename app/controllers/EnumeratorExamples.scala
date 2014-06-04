package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Promise
import play.api.libs.concurrent.Execution.Implicits._

object EnumeratorExamples extends Controller {
	def example1 = Action {
	  Ok.chunked(Enumerator("Created ", "using ", "Enumerator", ".apply()\n\n"))
	}

	def example2 = Action {
	  Ok.chunked(Enumerator.repeatM(Promise.timeout("Hello\n", 500)));
	}

	def example3 = Action {
	  val helloEnumerator = Enumerator("hello ")
	  val goodbyeEnumerator = Enumerator("goodbye\n\n")
	  val helloGoodbyeEnumerator = helloEnumerator.andThen(goodbyeEnumerator)
	  Ok.chunked(helloGoodbyeEnumerator)
	}

	def example4 = Action {
	  val helloEnumerator = Enumerator.repeatM(Promise.timeout("Hello\n", 500))
	  val goodbyeEnumerator = Enumerator.repeatM(Promise.timeout("Goodbye\n", 1000))
	  val helloGoodbyeEnumerator = Enumerator.interleave(helloEnumerator, goodbyeEnumerator)
	  Ok.chunked(helloGoodbyeEnumerator)
	}
}