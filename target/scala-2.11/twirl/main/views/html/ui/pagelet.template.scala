
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
object pagelet extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[Html,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(contents: Html, id: String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.30*/("""

"""),format.raw/*3.1*/("""<script type="text/html-stream" id=""""),_display_(/*3.38*/id),format.raw/*3.40*/("""-contents">
  """),_display_(/*4.4*/contents),format.raw/*4.12*/("""
"""),format.raw/*5.1*/("""</script>

<script type="text/javascript">
  document.getElementById(""""),_display_(/*8.29*/id),format.raw/*8.31*/("""").innerHTML = document.getElementById(""""),_display_(/*8.72*/id),format.raw/*8.74*/("""-contents").innerHTML;
</script>"""))}
  }

  def render(contents:Html,id:String): play.twirl.api.HtmlFormat.Appendable = apply(contents,id)

  def f:((Html,String) => play.twirl.api.HtmlFormat.Appendable) = (contents,id) => apply(contents,id)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Sun Mar 22 23:22:55 PDT 2015
                  SOURCE: D:/work/java/play/ping-play/app/views/ui/pagelet.scala.html
                  HASH: b45709ed2cd77a7657bd50329c0a7bb4311877f0
                  MATRIX: 573->1|689->29|717->31|780->68|802->70|842->85|870->93|897->94|994->165|1016->167|1083->208|1105->210
                  LINES: 21->1|24->1|26->3|26->3|26->3|27->4|27->4|28->5|31->8|31->8|31->8|31->8
                  -- GENERATED --
              */
          