package com.ybrikman.ping

import play.api.test.WithBrowser
import scala.collection.JavaConverters._

/**
 * An end-to-end test of the de-duping cache. The Scala and Java sample apps can extend this trait to run all the tests
 * in it.
 */
trait BaseDedupeSpec extends PingSpecification {
  "The Deduping controller" should {
    "dedupe remote calls" in new WithBrowser(app = createTestComponents().app) {
      browser.goTo(s"http://localhost:$port/dedupe")
      val values = browser.$(".id").getTexts.asScala

      // First 3 values should be the same since they were de-duped, fourth should be different
      values must have size 4
      values(0) mustEqual values(1)
      values(1) mustEqual values(2)
      values(1) mustNotEqual values(3)
    }
  }
}
