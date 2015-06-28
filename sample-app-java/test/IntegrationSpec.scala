import controllers.Mock
import org.specs2.mutable.Specification
import play.api.test.WithBrowser

class IntegrationSpec extends Specification {
  "Application" should {

    "render the non-streaming WVYP page" in new WithBrowser {
      browser.goTo(s"http://localhost:$port/wvyp")
      browser.$(".wvyp-count .large-number").getTexts.get(0) must equalTo(Mock.DEFAULT_WVYP_RESPONSE)
    }

    "render the streaming WVYP page" in new WithBrowser {
      browser.goTo(s"http://localhost:$port/stream")
      browser.$(".wvyp-count .large-number").getTexts.get(0) must equalTo(Mock.DEFAULT_WVYP_RESPONSE)
    }
  }
}
