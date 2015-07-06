package com.ybrikman.ping.scalaapi.bigpipe

import play.api.libs.iteratee.{Enumeratee, Enumerator}
import play.twirl.api.{Appendable, HtmlFormat, Format, Html}
import play.api.mvc.{Codec, Result}
import scala.language.implicitConversions
import scala.concurrent.{ExecutionContext, Future}

/**
 * A custom Appendable that lets you create .scala.stream templates instead of .scala.html. These templates can mix Html
 * markup with Enumerators that contain Html markup so that as soon as the content is available, Play can stream it
 * back to the client. You need to add this class as a custom template type in build.sbt.
 *
 * @param enumerator
 */
class HtmlStream(val enumerator: Enumerator[Html]) extends Appendable[HtmlStream] {
  def andThen(other: HtmlStream): HtmlStream = HtmlStream.fromHtmlEnumerator(enumerator.andThen(other.enumerator))
}

/**
 * Companion object for HtmlStream that contains convenient factory and composition methods.
 */
object HtmlStream {

  /**
   * Create an empty HtmlStream
   *
   * @return
   */
  def empty: HtmlStream = {
    fromString("")
  }

  /**
   * Create an HtmlStream from a String
   *
   * @param text
   * @return
   */
  def fromString(text: String): HtmlStream = {
    fromHtml(Html(text))
  }

  /**
   * Create an HtmlStream from a Future that will eventually contain a String
   *
   * @param eventuallyString
   * @return
   */
  def fromStringFuture(eventuallyString: Future[String])(implicit ec: ExecutionContext): HtmlStream = {
    fromHtmlFuture(eventuallyString.map(Html.apply))
  }

  /**
   * Create an HtmlStream from Html
   *
   * @param html
   * @return
   */
  def fromHtml(html: Html): HtmlStream = {
    fromHtmlEnumerator(Enumerator(html))
  }

  /**
   * Create an HtmlStream from an Enumerator of Html
   *
   * @param enumerator
   * @return
   */
  def fromHtmlEnumerator(enumerator: Enumerator[Html]): HtmlStream = {
    new HtmlStream(enumerator)
  }

  /**
   * Create an HtmlStream from a Future that will eventually contain Html
   *
   * @param eventuallyHtml
   * @return
   */
  def fromHtmlFuture(eventuallyHtml: Future[Html])(implicit ec: ExecutionContext): HtmlStream = {
    flatten(eventuallyHtml.map(fromHtml))
  }

  /**
   * Create an HtmlStream from the body of the Result.
   *
   * @param result
   * @return
   */
  def fromResult(result: Result)(implicit ec: ExecutionContext, codec: Codec): HtmlStream = {
    HtmlStream.fromHtmlEnumerator(result.body.map(bytes => Html(codec.decode(bytes))))
  }

  /**
   * Create an HtmlStream from a the body of a Future[Result].
   *
   * @param result
   * @return
   */
  def fromResultFuture(result: Future[Result])(implicit ec: ExecutionContext): HtmlStream = {
    flatten(result.map(fromResult))
  }

  /**
   * Interleave multiple HtmlStreams together. Interleaving is done based on whichever HtmlStream next has input ready,
   * if multiple have input ready, the order is undefined.
   *
   * @param streams
   * @return
   */
  def interleave(streams: HtmlStream*): HtmlStream = {
    fromHtmlEnumerator(Enumerator.interleave(streams.map(_.enumerator)))
  }

  /**
   * Create an HtmlStream from a Future that will eventually contain an HtmlStream.
   *
   * @param eventuallyStream
   * @return
   */
  def flatten(eventuallyStream: Future[HtmlStream])(implicit ec: ExecutionContext): HtmlStream = {
    fromHtmlEnumerator(Enumerator.flatten(eventuallyStream.map(_.enumerator)))
  }
}

/**
 * A custom Appendable that lets you create .scala.stream templates instead of .scala.html. These templates can mix Html
 * markup with Enumerators that contain Html markup so that as soon as the content is available, Play can stream it
 * back to the client.
 */
object HtmlStreamFormat extends Format[HtmlStream] {

  def raw(text: String): HtmlStream = {
    HtmlStream.fromString(text)
  }

  def escape(text: String): HtmlStream = {
    raw(HtmlFormat.escape(text).body)
  }

  def empty: HtmlStream = {
    raw("")
  }

  def fill(elements: scala.collection.immutable.Seq[HtmlStream]): HtmlStream = {
    elements.reduce((agg, curr) => agg.andThen(curr))
  }
}

/**
 * Useful implicits when working with HtmlStreams
 */
object HtmlStreamImplicits {

  /**
   * Implicit conversion so HtmlStream can be passed directly to Ok.feed and Ok.chunked
   *
   * @param stream
   * @param ec
   * @return
   */
  implicit def toEnumerator(stream: HtmlStream)(implicit ec: ExecutionContext): Enumerator[Html] = {
    // Skip empty chunks, as these mean EOF in chunked encoding
    stream.enumerator.through(Enumeratee.filter(!_.body.isEmpty))
  }
}

