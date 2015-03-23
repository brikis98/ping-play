
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
object wvypCount extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[Int,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(wvypCount: Int):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.18*/("""

"""),format.raw/*3.1*/("""<p class="wvypCount">
  <span class="large-number">"""),_display_(/*4.31*/wvypCount),format.raw/*4.40*/("""</span>
  <span>Your profile has been viewed by <b>"""),_display_(/*5.45*/wvypCount),format.raw/*5.54*/("""</b> people in the past 3 days</span>
</p>"""))}
  }

  def render(wvypCount:Int): play.twirl.api.HtmlFormat.Appendable = apply(wvypCount)

  def f:((Int) => play.twirl.api.HtmlFormat.Appendable) = (wvypCount) => apply(wvypCount)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Sun Mar 22 23:22:55 PDT 2015
                  SOURCE: D:/work/java/play/ping-play/app/views/pagelets/profiles/wvypCount.scala.html
                  HASH: ae208734117148995e5390a8a17ab755ba1c8219
                  MATRIX: 582->1|686->17|714->19|792->71|821->80|899->132|928->141
                  LINES: 21->1|24->1|26->3|27->4|27->4|28->5|28->5
                  -- GENERATED --
              */
          