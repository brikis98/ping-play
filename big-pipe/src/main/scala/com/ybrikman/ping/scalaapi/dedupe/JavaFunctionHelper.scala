package com.ybrikman.ping.scalaapi.dedupe

import java.util.function.{Consumer => JavaConsumer, Supplier => JavaSupplier}

object JavaFunctionHelper {
  def toScalaFunction[A](consumer: JavaConsumer[A]): A => Unit = {
    (a) => consumer.accept(a)
  }

  def toScalaFunction[A](supplier: JavaSupplier[A]): () => A = {
    () => supplier.get()
  }
}
