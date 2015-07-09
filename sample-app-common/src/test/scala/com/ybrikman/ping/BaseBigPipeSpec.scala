package com.ybrikman.ping

import play.api.test.WithBrowser

/**
 * End-to-end tests of BigPipe functionality. The Scala and Java sample apps can extend this trait to run all the tests
 * in it.
 */
trait BaseBigPipeSpec extends PingSpecification {
  "The sample app" should {
    "render the page without BigPipe" in new WithBrowser(app = createTestComponents().app) {
      browser.goTo(s"http://localhost:$port/withoutBigPipe")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
    }

    "render the page client-side with BigPipe" in new WithBrowser(app = createTestComponents().app) {
      browser.goTo(s"http://localhost:$port/withBigPipe")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
    }

    "render the page server-side with BigPipe" in new WithBrowser(app = createTestComponents().app) {
      browser.goTo(s"http://localhost:$port/serverSideRendering")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
    }

    "render the page client-side with BigPipe and Mustache.js JavaScript templates" in new WithBrowser(app = createTestComponents().app) {
      browser.goTo(s"http://localhost:$port/clientSideTemplating")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
    }

    "handle errors while rendering with BigPipe" in new WithBrowser(app = createTestComponents().app) {
      browser.goTo(s"http://localhost:$port/errorHandling")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
      browser.$("#feed .id").getTexts.get(0) must equalTo("error")
    }
  }
}
