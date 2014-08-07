package ui

import play.api.mvc.Result
import play.twirl.api.Html
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Some utilities for merging and de-duping static content from multiple modules
 */
object StaticContent {
  val cssHeaderName = "X-CSS"
  val jsHeaderName = "X-JS"

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
  def renderCssDependencies(css: Seq[String]): Html = views.html.ui.css(css)

  /**
   * Render the given sequence of JS URLs as script tags
   *
   * @param js
   * @return
   */
  def renderJsDependencies(js: Seq[String]): Html = views.html.ui.js(js)

  /**
   * Merge all the JavaScript dependencies from the results into a list of script tags
   *
   * @param results
   * @return
   */
  def mergeJsFromResults(results: Future[Result]*): HtmlStream = {
    mergeDependenciesFromResults(parseJsHeader, renderJsDependencies, results)
  }

  /**
   * Merge all the CSS dependencies from the results into a list of link tags
   *
   * @param results
   * @return
   */
  def mergeCssFromResults(results: Future[Result]*): HtmlStream = {
    mergeDependenciesFromResults(parseCssHeader, renderCssDependencies, results)
  }

  private def parseHeader(headerName: String, result: Result): Seq[String] = {
    result.header.headers.get(headerName).map(_.split(",").toVector).getOrElse(Vector.empty)
  }

  private def mergeDependenciesFromResults(parseHeader: Result => Seq[String], render: Seq[String] => Html, resultFutures: Seq[Future[Result]]): HtmlStream = {
    val allResultsFuture = Future.sequence(resultFutures)

    val htmlFuture = allResultsFuture.map { results =>
      val values = results.map(parseHeader).flatten.distinct
      render(values)
    }

    HtmlStream(htmlFuture)
  }
}
