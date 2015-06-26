package ui

import play.twirl.api.Html
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Cookie, Cookies, Codec, Result}
import play.api.libs.iteratee.Iteratee
import play.api.http.HeaderNames
import play.libs.F.Promise

/**
 * Helpers for working with small "pieces" of pages.
 */
object Pagelet {

  /**
   * Read the body of a Result as Html. Since the body is an Enumerator and may not be available yet, this method
   * returns a Future.
   *
   * @param result
   * @param codec
   * @return
   */
  def readBody(result: Result)(implicit codec: Codec): Future[Html] = {
    result.body.run(Iteratee.consume()).map(bytes => Html(new String(bytes, codec.charset)))
  }

  /**
   * Merge all the cookies set in the given results into a single sequence.
   *
   * @param results
   * @return
   */
  def mergeCookies(results: Result*): Seq[Cookie] = {
    results.flatMap(result => result.header.headers.get(HeaderNames.SET_COOKIE).map(Cookies.decodeSetCookieHeader).getOrElse(Seq.empty))
  }

  /**
   * Wrap the given Html in a script tag that will inject the Html into the DOM node with the given id
   *
   * @param html
   * @param id
   * @return
   */
  def render(html: Html, id: String): Html = {
    views.html.ui.pagelet(html, id)
  }

  /**
   * Wrap the given Html in a script tag that will inject the Html into the DOM node with the given id. Returns an
   * HtmlStream that can be used in a .scala.stream template.
   *
   * @param html
   * @param id
   * @return
   */
  def renderStream(html: Html, id: String): HtmlStream = {
    HtmlStream(render(html, id))
  }

  /**
   * Wrap the given Html in a script tag that will inject the Html into the DOM node with the given id. Returns an
   * HtmlStream that can be used in a .scala.stream template.
   *
   * @param htmlFuture
   * @param id
   * @return
   */
  def renderStream(htmlFuture: Future[Html], id: String): HtmlStream = {
    HtmlStream.flatten(htmlFuture.map(html => renderStream(html, id)))
  }

  /**
   * Java API. Wrap the given Html in a script tag that will inject the Html into the DOM node with the given id.
   * Returns an HtmlStream that can be used in a .scala.stream template.
   *
   * @param htmlPromise
   * @param id
   * @return
   */
  def renderStream(htmlPromise: Promise[Html], id: String): HtmlStream = {
    renderStream(htmlPromise.wrapped(), id)
  }
}
