package ui

import play.api.templates.Html
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Cookie, Cookies, Codec, SimpleResult}
import play.api.libs.iteratee.Iteratee
import play.api.http.HeaderNames

object Pagelet {

  def readBody(result: SimpleResult)(implicit codec: Codec): Future[Html] = {
    result.body.run(Iteratee.consume()).map(bytes => Html(new String(bytes, codec.charset)))
  }

  def mergeCookies(results: SimpleResult*): Seq[Cookie] = {
    results.flatMap(result => result.header.headers.get(HeaderNames.SET_COOKIE).map(Cookies.decode).getOrElse(Seq.empty))
  }

  def render(html: Html, id: String): Html = {
    views.html.ui.pagelet(html, id)
  }

  def renderStream(html: Html, id: String): HtmlStream = {
    HtmlStream(render(html, id))
  }

  def renderStream(htmlFuture: Future[Html], id: String): HtmlStream = {
    HtmlStream.flatten(htmlFuture.map(html => renderStream(html, id)))
  }
}
