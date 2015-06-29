package com.ybrikman.ping.scalaapi.bigpipe

import javax.script.{ScriptEngine, ScriptEngineManager}

import jdk.nashorn.api.scripting.JSObject
import org.specs2.mutable.Specification
import play.api.libs.json.Json

import scala.io.Source

class TestBigPipeJavaScript extends Specification {

  private val BigPipeJsPath = "public/com/ybrikman/pingplay/big-pipe.js"

  "big-pipe.js should" >> {
    "be able to unescape HTML escaped by Embed.escapeForEmbedding" >> {
      val html = "<h1>Hello World</h1><!-- The dashes in this comment should be escaped -->"
      val escapedHtml = Embed.escapeForEmbedding(html).body

      val engine = getJsEngineWithBigPipeLoaded
      val result = engine.eval(s"""BigPipe.unescapeForEmbedding('$escapedHtml')""")
      result.toString mustEqual html
    }

    "be able to unescape JSON escaped by Embed.escapeForEmbedding" >> {
      val data = Map("foo" -> "bar", "baz--" -> "--blah--")
      val json = Json.stringify(Json.toJson(data))
      val escapedJson = Embed.escapeForEmbedding(json).body

      val engine = getJsEngineWithBigPipeLoaded
      val result = engine.eval(
        s"""
           |BigPipe.readEmbeddedContentFromDom = function(domId) {
           |  return '$escapedJson';
           |};
           |BigPipe.parseEmbeddedJsonFromDom('foo')""".stripMargin).asInstanceOf[JSObject]

      result.getMember("foo") mustEqual data("foo")
      result.getMember("baz--") mustEqual data("baz--")
    }

    "ignore null content when unescaping" >> {
      val engine = getJsEngineWithBigPipeLoaded
      engine.eval("""BigPipe.unescapeForEmbedding(null)""") must beNull
    }
  }

  private def getJsEngineWithBigPipeLoaded: ScriptEngine = {
    // Must create the ScriptEngineManager with the class loader or getEngineByName will return null in SBT
    // See: https://github.com/sbt/sbt/issues/1214#issuecomment-55566056
    val engine = new ScriptEngineManager(getClass.getClassLoader).getEngineByName("nashorn")
    engine.eval(getBigPipeJs)
    engine
  }

  private def getBigPipeJs: String = {
    val inputStream = getClass.getClassLoader.getResourceAsStream(BigPipeJsPath)
    Source.fromInputStream(inputStream).mkString
  }
}
