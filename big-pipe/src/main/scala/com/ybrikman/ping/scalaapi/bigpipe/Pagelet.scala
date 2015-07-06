package com.ybrikman.ping.scalaapi.bigpipe

import com.ybrikman.ping.javaapi.bigpipe.PageletContentType
import play.api.libs.json.{JsValue, Json}
import play.twirl.api.Html

import scala.concurrent.{ExecutionContext, Future}
import PageletConstants._

/**
 * The base trait for "pagelets", which represent small, self-contained pieces of a page that can be rendered
 * independently. 
 */
trait Pagelet {
  /**
   * A unique id for this Pagelet. Usually corresponds to the id in the DOM where this Pagelet should be inserted.
   */
  val id: String

  /**
   * Render an HTML placeholder for this Pagelet. This will be filled in later using JavaScript code when the Pagelet
   * data is available and shows up in the browser.
   * 
   * @param ec
   * @return
   */
  def renderPlaceholder(implicit ec: ExecutionContext): HtmlStream = {
    HtmlStream.fromHtml(com.ybrikman.bigpipe.html.pageletServerSide(id, EmptyContent))
  }

  /**
   * Render all the HTML for this Pagelet server-side. This is typically used when the Pagelets are being streamed 
   * in-order, which is useful for clients that do not support JavaScript and search engine crawlers (i.e. SEO).
   * 
   * @param ec
   * @return
   */
  def renderServerSide(implicit ec: ExecutionContext): HtmlStream

  /**
   * Render the HTML for this Pagelet so that it's initially invisible and can be inserted into the proper place in the
   * DOM client-side, using JavaScript. This is typically used when the Pagelets are being streamed out-of-order to 
   * minimize the load-time for a page.
   * 
   * @param ec
   * @return
   */
  def renderClientSide(implicit ec: ExecutionContext): HtmlStream
}

/**
 * A Pagelet that contains HTML. Both server-side and client-side rendering are supported.
 * 
 * @param id
 * @param content
 */
case class HtmlPagelet(id: String, content: Future[Html]) extends Pagelet {
  override def renderServerSide(implicit ec: ExecutionContext): HtmlStream = {
    HtmlStream.fromHtmlFuture(content.map(str => com.ybrikman.bigpipe.html.pageletServerSide(id, str.body)))
  }

  override def renderClientSide(implicit ec: ExecutionContext): HtmlStream = {
    HtmlStream.fromHtmlFuture(content.map(str =>
      com.ybrikman.bigpipe.html.pageletClientSide(str.body, id, PageletContentType.html)))
  }
}

/**
 * A Pagelet that contains JSON. The general usage pattern is to send this JSON to the browser and render it using a
 * client-side templating language, such as Mustache.js. Therefore, this Pagelet only supports client-side rendering
 * and will throw an exception if you try to render it server-side.
 * 
 * @param id
 * @param content
 */
case class JsonPagelet(id: String, content: Future[JsValue]) extends Pagelet {
  override def renderServerSide(implicit ec: ExecutionContext): HtmlStream = {
    throw new UnsupportedOperationException(s"Server-side rendering is not supported for ${getClass.getName}")
  }

  override def renderClientSide(implicit ec: ExecutionContext): HtmlStream = {
    HtmlStream.fromHtmlFuture(content.map(json =>
      com.ybrikman.bigpipe.html.pageletClientSide(Json.stringify(json), id, PageletContentType.json)))
  }
}

/**
 * A Pagelet that contains plain text. Both server-side and client-side rendering are supported.
 * 
 * @param id
 * @param content
 */
case class TextPagelet(id: String, content: Future[String]) extends Pagelet {
  override def renderServerSide(implicit ec: ExecutionContext): HtmlStream = {
    HtmlStream.fromHtmlFuture(content.map(str => com.ybrikman.bigpipe.html.pageletServerSide(id, str)))
  }

  override def renderClientSide(implicit ec: ExecutionContext): HtmlStream = {
    HtmlStream.fromHtmlFuture(content.map(str =>
      com.ybrikman.bigpipe.html.pageletClientSide(str, id, PageletContentType.text)))
  }
}

object PageletConstants {
  val EmptyContent = ""
}