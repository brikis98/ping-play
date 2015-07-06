package com.ybrikman.ping.scalaapi.bigpipe

import com.ybrikman.ping.javaapi.bigpipe.PageletRenderOptions
import com.ybrikman.ping.javaapi.bigpipe.PageletRenderOptions._

import scala.concurrent.ExecutionContext

/**
 * This class composes the given Pagelets together and prepares them for either out-of-order client-side rendering (if
 * renderOptions is set to ClientSide) or in-order server-side rendering (if renderOptions is set to ServerSide). Use
 * the render method in this class in your templates to actually render the Pagelets. It provides you a Map from
 * Pagelet id to the HtmlStream for that Pagelet. Insert the HtmlStream in this Map for each Pagelet into the
 * appropriate part of your markup.
 *
 * @param renderOptions
 * @param pagelets
 * @param ec
 */
class BigPipe(renderOptions: PageletRenderOptions, pagelets: Pagelet*)(implicit ec: ExecutionContext) {

  /**
   * Render the Pagelets in this BigPipe. The layoutBody function will get as an argument a Map from Pagelet id to
   * HtmlStream for that Pagelet. Insert this HtmlStream into the appropriate place in your markup.
   *
   * @param layoutBody
   * @return
   */
  def render(layoutBody: Map[String, HtmlStream] => HtmlStream): HtmlStream = {
    val bodyPagelets = pagelets.map { pagelet =>
      renderOptions match {
        case ClientSide => pagelet.id -> pagelet.renderPlaceholder
        case ServerSide => pagelet.id -> pagelet.renderServerSide
      }
    }.toMap

    val footerPagelets = renderOptions match {
      case ClientSide => HtmlStream.interleave(pagelets.map(_.renderClientSide):_*)
      case ServerSide => HtmlStream.empty
    }

    layoutBody(bodyPagelets).andThen(footerPagelets)
  }
}

