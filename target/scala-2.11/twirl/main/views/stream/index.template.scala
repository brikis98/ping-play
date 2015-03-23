
package views.stream

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
object index extends BaseScalaTemplate[ui.HtmlStreamFormat.Appendable,Format[ui.HtmlStreamFormat.Appendable]](ui.HtmlStreamFormat) with play.twirl.api.Template1[HtmlStream,ui.HtmlStreamFormat.Appendable] {

  /**/
  def apply/*1.2*/(pipe: HtmlStream):ui.HtmlStreamFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.20*/("""

"""),format.raw/*3.1*/("""<html>
    <head>
        <link rel="stylesheet" href="/assets/stylesheets/main.css"/>
    </head>
    <body>

        """),_display_(/*9.10*/views/*9.15*/.stream.pagelets.updates.updates()),format.raw/*9.49*/("""
        """),_display_(/*10.10*/views/*10.15*/.stream.pagelets.profiles.profile()),format.raw/*10.50*/("""

    """),format.raw/*12.5*/("""<script src="/assets/javascripts/jquery-1.9.0.min.js"></script>

        """),_display_(/*14.10*/pipe),format.raw/*14.14*/("""

    """),format.raw/*16.5*/("""</body>
</html>"""))}
  }

  def render(pipe:HtmlStream): ui.HtmlStreamFormat.Appendable = apply(pipe)

  def f:((HtmlStream) => ui.HtmlStreamFormat.Appendable) = (pipe) => apply(pipe)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Sun Mar 22 23:22:55 PDT 2015
                  SOURCE: D:/work/java/play/ping-play/app/views/index.scala.stream
                  HASH: 0e56b1ab78d02a127194ca65b3f983bc2f402704
                  MATRIX: 547->1|647->19|675->21|821->141|834->146|888->180|925->190|939->195|995->230|1028->236|1129->310|1154->314|1187->320
                  LINES: 21->1|24->1|26->3|32->9|32->9|32->9|33->10|33->10|33->10|35->12|37->14|37->14|39->16
                  -- GENERATED --
              */
          