package ui

import play.api.mvc.SimpleResult
import play.api.templates.Html
import scala.concurrent.Future
import ui.HtmlStream
import play.api.libs.concurrent.Execution.Implicits._

object StaticContent {
  val cssHeaderName = "X-CSS"
  val jsHeaderName = "X-JS"

  def asHeaders(css: Seq[String], js: Seq[String]): Seq[(String, String)] = {
    Seq(cssHeaderName -> css.mkString(","), jsHeaderName -> js.mkString(","))
  }

  def mergeCssHeaders(results: SimpleResult*): Seq[String] = mergeHeaderValues(cssHeaderName, results:_*)

  def mergeJsHeaders(results: SimpleResult*): Seq[String] = mergeHeaderValues(jsHeaderName, results:_*)

  private def mergeHeaderValues(headerName: String, results: SimpleResult*): Seq[String] = {
    results.flatMap { result =>
      result.header.headers.get(headerName).map(_.split(",").toVector).getOrElse(Seq.empty)
    }.distinct
  }

  def parseCssHeader(result: SimpleResult): Seq[String] = parseHeader(cssHeaderName, result)

  def parseJsHeader(result: SimpleResult): Seq[String] = parseHeader(jsHeaderName, result)

  def renderCssDependencies(css: Seq[String]): Html = views.html.ui.css(css)

  def renderJsDependencies(js: Seq[String]): Html = views.html.ui.js(js)

  def mergeJsFromResults(results: Future[SimpleResult]*): HtmlStream = {
    mergeDependenciesFromResults(parseJsHeader, renderJsDependencies, results)
  }

  def mergeCssFromResults(results: Future[SimpleResult]*): HtmlStream = {
    mergeDependenciesFromResults(parseCssHeader, renderCssDependencies, results)
  }

  private def parseHeader(headerName: String, result: SimpleResult): Seq[String] = {
    result.header.headers.get(headerName).map(_.split(",").toVector).getOrElse(Vector.empty)
  }

  private def mergeDependenciesFromResults(parseHeader: SimpleResult => Seq[String], render: Seq[String] => Html, resultFutures: Seq[Future[SimpleResult]]): HtmlStream = {
    val allResultsFuture = Future.sequence(resultFutures)

    val htmlFuture = allResultsFuture.map { results =>
      val values = results.map(parseHeader).flatten.distinct
      render(values)
    }

    HtmlStream(htmlFuture)
  }
}
