package com.ybrikman.ping.scalaapi.bigpipe

import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.mvc.Results.Chunks.Out

import scala.concurrent.ExecutionContext

object JavaStreamHelper {

  def writeEnumeratorToOut[A](enumerator: Enumerator[A], out: Out[A], executionContext: ExecutionContext): Unit = {
    implicit val ec = executionContext
    enumerator.run(Iteratee.foreach { chunk =>
      if (!chunk.toString.isEmpty) {
        out.write(chunk)
      }
    }).onComplete(_ => out.close())
  }
}
