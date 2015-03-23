
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
object css extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[Seq[String],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(urls: Seq[String]):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.21*/("""

"""),_display_(/*3.2*/for(url <- urls) yield /*3.18*/ {_display_(Seq[Any](format.raw/*3.20*/("""
  """),format.raw/*4.3*/("""<link rel="stylesheet" href=""""),_display_(/*4.33*/url),format.raw/*4.36*/(""""/>
""")))}))}
  }

  def render(urls:Seq[String]): play.twirl.api.HtmlFormat.Appendable = apply(urls)

  def f:((Seq[String]) => play.twirl.api.HtmlFormat.Appendable) = (urls) => apply(urls)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Sun Mar 22 23:22:55 PDT 2015
                  SOURCE: D:/work/java/play/ping-play/app/views/ui/css.scala.html
                  HASH: 3cdcda9bc5dd11fdfe95a5f6c06205f9567d1741
                  MATRIX: 569->1|676->20|704->23|735->39|774->41|803->44|859->74|882->77
                  LINES: 21->1|24->1|26->3|26->3|26->3|27->4|27->4|27->4
                  -- GENERATED --
              */
          