package com.ybrikman.ping.scalaapi.bigpipe

import com.ybrikman.ping.javaapi.bigpipe.PageletContentType
import play.api.libs.json.{Json, JsValue}
import play.twirl.api.Html

import scala.concurrent.{ExecutionContext, Future}

/**
 * Create a "pagelet" that can be sent down the browser as soon as the given content is available and rendered
 * client-side into the correct spot on the page, as identified by the given DOM id. This is done by:
 *
 * 1. Wrapping the given content in HTML markup that will not be visible when the browser first processes it
 * 2. Adding JavaScript that will extract the hidden content and insert it into the proper location in the DOM
 *
 * Use the Pagelet.fromXXX methods to create Pagelets from a variety of types. Use the Pagelet.asHtmlXXX methods to
 * get the HTML/JS code that you should insert into your page.
 *
 * @param content
 * @param id
 * @param contentType
 * @param ec
 */
class Pagelet(content: Future[String], id: String, contentType: PageletContentType, ec: ExecutionContext) {

  def asHtmlFuture: Future[Html] = {
    content.map(str => com.ybrikman.bigpipe.html.embedHtml(str, id, contentType))(ec)
  }

  def asHtmlStream: HtmlStream = {
    HtmlStream.fromHtmlFuture(asHtmlFuture)(ec)
  }
}

object Pagelet {

  def fromStringFuture(content: Future[String], id: String)(implicit ec: ExecutionContext): Pagelet = {
    new Pagelet(content, id, PageletContentType.text, ec)
  }

  def fromString(content: String, id: String)(implicit ec: ExecutionContext): Pagelet = {
    fromStringFuture(Future.successful(content), id)
  }

  def fromHtmlFuture(content: Future[Html], id: String)(implicit ec: ExecutionContext): Pagelet = {
    new Pagelet(content.map(html => html.body), id, PageletContentType.html, ec)
  }

  def fromHtml(content: Html, id: String)(implicit ec: ExecutionContext): Pagelet = {
    fromHtmlFuture(Future.successful(content), id)
  }

  def fromJsonFuture(content: Future[JsValue], id: String)(implicit ec: ExecutionContext): Pagelet = {
    new Pagelet(content.map(json => Json.stringify(json)), id, PageletContentType.json, ec)
  }

  def fromJson(content: JsValue, id: String)(implicit ec: ExecutionContext): Pagelet = {
    fromJsonFuture(Future.successful(content), id)
  }
}
