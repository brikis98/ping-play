
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
object page extends BaseScalaTemplate[ui.HtmlStreamFormat.Appendable,Format[ui.HtmlStreamFormat.Appendable]](ui.HtmlStreamFormat) with play.twirl.api.Template2[HtmlStream,HtmlStream,ui.HtmlStreamFormat.Appendable] {

  /**/
  def apply/*1.2*/(holder: HtmlStream, pipe: HtmlStream):ui.HtmlStreamFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.40*/("""

"""),format.raw/*3.1*/("""<html>
    <head>
        <link rel="stylesheet" href="/assets/stylesheets/main.css"/>
    </head>
    <body>

        """),_display_(/*9.10*/holder),format.raw/*9.16*/("""

        """),format.raw/*11.9*/("""<script src="/assets/javascripts/jquery-1.9.0.min.js"></script>

        """),_display_(/*13.10*/pipe),format.raw/*13.14*/("""

    """),format.raw/*15.5*/("""</body>
</html>
"""))}
  }

  def render(holder:HtmlStream,pipe:HtmlStream): ui.HtmlStreamFormat.Appendable = apply(holder,pipe)

  def f:((HtmlStream,HtmlStream) => ui.HtmlStreamFormat.Appendable) = (holder,pipe) => apply(holder,pipe)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Mon Mar 23 20:57:05 PDT 2015
                  SOURCE: D:/work/java/play/ping-play/app/views/page.scala.stream
                  HASH: 91befc7db9159d3ce05999fcbbd3338152e205d2
                  MATRIX: 557->1|677->39|705->41|851->161|877->167|914->177|1015->251|1040->255|1073->261
                  LINES: 21->1|24->1|26->3|32->9|32->9|34->11|36->13|36->13|38->15
                  -- GENERATED --
              */
          