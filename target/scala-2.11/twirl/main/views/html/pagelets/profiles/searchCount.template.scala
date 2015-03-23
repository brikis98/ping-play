
package views.html.pagelets.profiles

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
object searchCount extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[Int,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(searchCount: Int):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.20*/("""

"""),format.raw/*3.1*/("""<p class="search-count">
  <span class="large-number">"""),_display_(/*4.31*/searchCount),format.raw/*4.42*/("""</span>
  <span>Your have shown up in search results <b>"""),_display_(/*5.50*/searchCount),format.raw/*5.61*/("""</b> times in the past 3 days</span>
</p>"""))}
  }

  def render(searchCount:Int): play.twirl.api.HtmlFormat.Appendable = apply(searchCount)

  def f:((Int) => play.twirl.api.HtmlFormat.Appendable) = (searchCount) => apply(searchCount)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Sun Mar 22 23:22:55 PDT 2015
                  SOURCE: D:/work/java/play/ping-play/app/views/pagelets/profiles/searchCount.scala.html
                  HASH: ef6ec1faf0999375ace678336ad785e893ba3e74
                  MATRIX: 584->1|690->19|718->21|799->76|830->87|913->144|944->155
                  LINES: 21->1|24->1|26->3|27->4|27->4|28->5|28->5
                  -- GENERATED --
              */
          