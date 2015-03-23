
package views.html.ui

import play.twirl.api._
import play.twirl.api.TemplateMagic._

import play.api.templates.PlayMagic._
import models._
import controllers._
import play.api.i18n._
import play.api.mvc._
import play.api.data._
import views.html._
import _root_.ui.HtmlStream
import _root_.ui.HtmlStream._

/**/
object js extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[Seq[String],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(urls: Seq[String]):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.21*/("""

"""),_display_(/*3.2*/for(url <- urls) yield /*3.18*/ {_display_(Seq[Any](format.raw/*3.20*/("""
  """),format.raw/*4.3*/("""<script src=""""),_display_(/*4.17*/url),format.raw/*4.20*/("""" type="text/javascript"></script>
""")))}))}
  }

  def render(urls:Seq[String]): play.twirl.api.HtmlFormat.Appendable = apply(urls)

  def f:((Seq[String]) => play.twirl.api.HtmlFormat.Appendable) = (urls) => apply(urls)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Sun Mar 22 23:22:55 PDT 2015
                  SOURCE: D:/work/java/play/ping-play/app/views/ui/js.scala.html
                  HASH: 54aa3c5c80e874b449f194d4214396fab95feba0
                  MATRIX: 568->1|675->20|703->23|734->39|773->41|802->44|842->58|865->61
                  LINES: 21->1|24->1|26->3|26->3|26->3|27->4|27->4|27->4
                  -- GENERATED --
              */
          