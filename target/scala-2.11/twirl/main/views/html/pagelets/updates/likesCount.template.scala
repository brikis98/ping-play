
package views.html.pagelets.updates

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
object likesCount extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[Int,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(count: Int):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.14*/("""

"""),format.raw/*3.1*/("""<span class="large-number">"""),_display_(/*3.29*/count),format.raw/*3.34*/("""</span>
<span>You got <b>"""),_display_(/*4.19*/count),format.raw/*4.24*/("""</b> likes in the past 3 days</span>
"""))}
  }

  def render(count:Int): play.twirl.api.HtmlFormat.Appendable = apply(count)

  def f:((Int) => play.twirl.api.HtmlFormat.Appendable) = (count) => apply(count)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Sun Mar 22 23:22:55 PDT 2015
                  SOURCE: D:/work/java/play/ping-play/app/views/pagelets/updates/likesCount.scala.html
                  HASH: 07832c4d22008180032866f21a41315bcea913b5
                  MATRIX: 582->1|682->13|710->15|764->43|789->48|841->74|866->79
                  LINES: 21->1|24->1|26->3|26->3|26->3|27->4|27->4
                  -- GENERATED --
              */
          