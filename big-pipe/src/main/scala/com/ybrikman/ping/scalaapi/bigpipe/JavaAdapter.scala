package com.ybrikman.ping.scalaapi.bigpipe

import java.util.function.{Supplier => JavaSupplier, Consumer => JavaConsumer, Function => JavaFunction}

import com.fasterxml.jackson.databind.JsonNode
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.{Writes, JsValue}
import play.mvc.Results.Chunks.Out

import scala.concurrent.ExecutionContext

/**
 * A helper class for going between Scala and Java code
 */
object JavaAdapter {

  def writeEnumeratorToOut[A](enumerator: Enumerator[A], out: Out[A], executionContext: ExecutionContext): Unit = {
    implicit val ec = executionContext
    enumerator.run(Iteratee.foreach { chunk =>
      if (!chunk.toString.isEmpty) {
        out.write(chunk)
      }
    }).onComplete(_ => out.close())
  }
  
  def jsonNodeToJsValue(jsonNode: JsonNode): JsValue = {
    Writes.JsonNodeWrites.writes(jsonNode)
  }

  def javaConsumerToScalaFunction[A](consumer: JavaConsumer[A]): A => Unit = {
    (a) => consumer.accept(a)
  }

  def javaSupplierToScalaFunction[A](supplier: JavaSupplier[A]): () => A = {
    () => supplier.get()
  }

  def javaFunctionToScalaFunction[A, B](function: JavaFunction[A, B]): A => B = {
    (a) => function.apply(a)
  }
}
