package ui

import play.api.libs.iteratee.{Enumeratee, Enumerator}
import play.api.templates.{HtmlFormat, Html}
import play.templates.{Format, Appendable}
import scala.concurrent.Future
import play.api.mvc.SimpleResult
import play.api.libs.concurrent.Execution.Implicits._

case class HtmlStream(enumerator: Enumerator[Html]) extends Appendable[HtmlStream] {
  def +=(other: HtmlStream): HtmlStream = andThen(other)

  def andThen(other: HtmlStream): HtmlStream = HtmlStream(enumerator.andThen(other.enumerator))
}

object HtmlStream {

  def apply(text: String): HtmlStream = {
    apply(Html(text))
  }

  def apply(html: Html): HtmlStream = {
    HtmlStream(Enumerator(html))
  }

  def apply(eventuallyHtml: Future[Html]): HtmlStream = {
    flatten(eventuallyHtml.map(apply))
  }

  def fromResult(result: SimpleResult): HtmlStream = {
    HtmlStream(result.body.map(bytes => Html(new String(bytes, "UTF-8"))))
  }

  def fromResult(result: Future[SimpleResult]): HtmlStream = {
    flatten(result.map(fromResult))
  }

  def interleave(streams: HtmlStream*): HtmlStream = {
    HtmlStream(Enumerator.interleave(streams.map(_.enumerator)))
  }

  def flatten(eventuallyStream: Future[HtmlStream]): HtmlStream = {
    HtmlStream(Enumerator.flatten(eventuallyStream.map(_.enumerator)))
  }
}

object HtmlStreamFormat extends Format[HtmlStream] {

  def raw(text: String): HtmlStream = {
    HtmlStream(text)
  }

  def escape(text: String): HtmlStream = {
    raw(HtmlFormat.escape(text).body)
  }
}

object HtmlStreamImplicits {

  // Implicit conversion so HtmlStream can be passed directly to Ok.feed and Ok.chunked
  implicit def toEnumerator(stream: HtmlStream): Enumerator[Html] = {
    // Skip empty chunks, as these mean EOF in chunked encoding
    stream.enumerator.through(Enumeratee.filter(!_.body.isEmpty))
  }
}
