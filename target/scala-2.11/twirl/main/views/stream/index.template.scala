
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
object index extends BaseScalaTemplate[ui.HtmlStreamFormat.Appendable,Format[ui.HtmlStreamFormat.Appendable]](ui.HtmlStreamFormat) with play.twirl.api.Template3[HtmlStream,HtmlStream,HtmlStream,ui.HtmlStreamFormat.Appendable] {

  /**/
  def apply/*1.2*/(holder1: HtmlStream, holder2: HtmlStream, pipe: HtmlStream):ui.HtmlStreamFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.62*/("""

"""),format.raw/*3.1*/("""<html>
    <head>
        <link rel="stylesheet" href="/assets/stylesheets/main.css"/>
    </head>
    <body>

        """),_display_(/*9.10*/holder1),format.raw/*9.17*/("""
        """),_display_(/*10.10*/holder2),format.raw/*10.17*/("""

    """),format.raw/*12.5*/("""<script src="/assets/javascripts/jquery-1.9.0.min.js"></script>

        """),_display_(/*14.10*/pipe),format.raw/*14.14*/("""

    """),format.raw/*16.5*/("""</body>
</html>"""))}
  }

  def render(holder1:HtmlStream,holder2:HtmlStream,pipe:HtmlStream): ui.HtmlStreamFormat.Appendable = apply(holder1,holder2,pipe)

  def f:((HtmlStream,HtmlStream,HtmlStream) => ui.HtmlStreamFormat.Appendable) = (holder1,holder2,pipe) => apply(holder1,holder2,pipe)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Mon Mar 23 21:14:44 PDT 2015
                  SOURCE: D:/work/java/play/ping-play/app/views/index.scala.stream
                  HASH: 3921c7e0ac97548c32b55b15d6700eb2bc0c956a
                  MATRIX: 569->1|711->61|739->63|885->183|912->190|949->200|977->207|1010->213|1111->287|1136->291|1169->297
                  LINES: 21->1|24->1|26->3|32->9|32->9|33->10|33->10|35->12|37->14|37->14|39->16
                  -- GENERATED --
              */
          