package com.ybrikman.ping.scalaapi.compose

import com.ybrikman.ping.scalaapi.bigpipe.HtmlStream
import play.api.http.HeaderNames
import play.api.libs.iteratee.Iteratee
import play.api.mvc.{Cookies, Cookie, Codec, Result}
import play.twirl.api.Html

import scala.concurrent.{Future, ExecutionContext}

/**
 * Helpers for building Play apps out of composable controllers.
 * Note: these are not yet tested or documented, so use at your own risk.
 */
object Compose {

  val cssHeaderName = "X-CSS"
  val jsHeaderName = "X-JS"

  /**
   * Read the body of a Result as Html. Since the body is an Enumerator and may not be available yet, this method
   * returns a Future.
   *
   * @param result
   * @param codec
   * @return
   */
  def readBody(result: Result)(implicit codec: Codec, ec: ExecutionContext): Future[Html] = {
    result.body.run(Iteratee.consume()).map(bytes => Html(new String(bytes, codec.charset)))
  }

  /**
   * Merge all the cookies set in the given results into a single sequence.
   *
   * @param results
   * @return
   */
  def mergeCookies(results: Result*): Seq[Cookie] = {
    results
      .flatMap(result => result.header.headers.get(HeaderNames.SET_COOKIE)
      .map(Cookies.decodeSetCookieHeader)
      .getOrElse(Seq.empty))
  }

  /**
   * Convert the given sequences of CSS and JS into HTTP headers that can be added to the Result
   *
   * @param css
   * @param js
   * @return
   */
  def asHeaders(css: Seq[String], js: Seq[String]): Seq[(String, String)] = {
    Seq(cssHeaderName -> css.mkString(","), jsHeaderName -> js.mkString(","))
  }

  /**
   * Read the CSS header from each result and merge and de-dupe them into a single sequence
   *
   * @param results
   * @return
   */
  def mergeCssHeaders(results: Result*): Seq[String] = {
    mergeHeaderValues(cssHeaderName, parseCssHeader, results:_*)
  }

  /**
   * Read the JS header from each the result and merge and de-dupe them into a single sequence
   *
   * @param results
   * @return
   */
  def mergeJsHeaders(results: Result*): Seq[String] = {
    mergeHeaderValues(jsHeaderName, parseJsHeader, results:_*)
  }

  private def mergeHeaderValues(headerName: String, parseHeader: Result => Seq[String], results: Result*): Seq[String] = {
    results.flatMap(parseHeader).distinct
  }

  /**
   * Read the CSS header from the given Result, which should define the CSS dependencies for the Result
   *
   * @param result
   * @return
   */
  def parseCssHeader(result: Result): Seq[String] = parseHeader(cssHeaderName, result)

  /**
   * Read the JS header from the given Result, which should define the CSS dependencies for the Result
   *
   * @param result
   * @return
   */
  def parseJsHeader(result: Result): Seq[String] = parseHeader(jsHeaderName, result)

  /**
   * Render the given sequence of CSS URLs as link tags
   *
   * @param css
   * @return
   */
  def renderCssDependencies(css: Seq[String]): Html = {
    // TODO: views.html.ui.css(css)
    null
  }

  /**
   * Render the given sequence of JS URLs as script tags
   *
   * @param js
   * @return
   */
  def renderJsDependencies(js: Seq[String]): Html = {
    // TODO: views.html.ui.js(js)
    null
  }

  /**
   * Merge all the JavaScript dependencies from the results into a list of script tags
   *
   * @param results
   * @return
   */
  def mergeJsFromResults(results: Future[Result]*)(implicit ec: ExecutionContext): HtmlStream = {
    mergeDependenciesFromResults(parseJsHeader, renderJsDependencies, results)
  }

  /**
   * Merge all the CSS dependencies from the results into a list of link tags
   *
   * @param results
   * @return
   */
  def mergeCssFromResults(results: Future[Result]*)(implicit ec: ExecutionContext): HtmlStream = {
    mergeDependenciesFromResults(parseCssHeader, renderCssDependencies, results)
  }

  private def parseHeader(headerName: String, result: Result): Seq[String] = {
    result.header.headers.get(headerName).map(_.split(",").toVector).getOrElse(Vector.empty)
  }

  private def mergeDependenciesFromResults(parseHeader: Result => Seq[String], render: Seq[String] => Html, resultFutures: Seq[Future[Result]])(implicit ec: ExecutionContext): HtmlStream = {
    val allResultsFuture = Future.sequence(resultFutures)

    val htmlFuture = allResultsFuture.map { results =>
      val values = results.map(parseHeader).flatten.distinct
      render(values)
    }

    HtmlStream.fromHtmlFuture(htmlFuture)
  }
}
