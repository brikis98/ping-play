
package views.stream.pagelets.profiles

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
object profile extends BaseScalaTemplate[ui.HtmlStreamFormat.Appendable,Format[ui.HtmlStreamFormat.Appendable]](ui.HtmlStreamFormat) with play.twirl.api.Template0[ui.HtmlStreamFormat.Appendable] {

  /**/
  def apply/*1.2*/():ui.HtmlStreamFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.4*/("""
"""),format.raw/*2.1*/("""<div class="wvyp">
    <h2>Who's Viewed Your Profile</h2>

    <div id="wvypCount"></div>
    <div id="searchCount"></div>
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
                  SOURCE: D:/work/java/play/ping-play/app/views/pagelets/profiles/profile.scala.stream
                  HASH: a0c0553cce403d2ab3924bbcb9bad9a008c1691d
                  MATRIX: 556->1|639->3|666->4
                  LINES: 21->1|24->1|25->2
                  -- GENERATED --
              */
          