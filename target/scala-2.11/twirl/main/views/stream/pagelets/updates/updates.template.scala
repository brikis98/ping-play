
package views.stream.pagelets.updates

import play.twirl.api._
import play.twirl.api.TemplateMagic._

import play.api.templates.PlayMagic._
import models._
import controllers._
import play.api.i18n._
import play.api.mvc._
import play.api.data._
import views.stream._
import _root_.ui.HtmlStream
import _root_.ui.HtmlStream._

/**/
object updates extends BaseScalaTemplate[ui.HtmlStreamFormat.Appendable,Format[ui.HtmlStreamFormat.Appendable]](ui.HtmlStreamFormat) with play.twirl.api.Template0[ui.HtmlStreamFormat.Appendable] {

  /**/
  def apply/*1.2*/():ui.HtmlStreamFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.4*/("""

"""),format.raw/*3.1*/("""<div class="wvyp">
    <h2>Who's Viewed Your Update</h2>
    <div id="likeCount"></div>
</div>
"""))}
  }

  def render(): ui.HtmlStreamFormat.Appendable = apply()

  def f:(() => ui.HtmlStreamFormat.Appendable) = () => apply()

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Sun Mar 22 23:22:55 PDT 2015
                  SOURCE: D:/work/java/play/ping-play/app/views/pagelets/updates/updates.scala.stream
                  HASH: 3f7c146d04d198a71c1eb6da7577ccc5718bc8ae
                  MATRIX: 555->1|638->3|666->5
                  LINES: 21->1|24->1|26->3
                  -- GENERATED --
              */
          