import org.specs2.mutable.Specification
import play.api.test.WithBrowser

class IntegrationSpec extends Specification {
  "Application" should {

    "render the non-streaming WVYP page" in new WithBrowser {
      browser.goTo(s"http://localhost:$port/withoutBigPipe")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
    }

    "render the streaming WVYP page" in new WithBrowser {
      browser.goTo(s"http://localhost:$port/withBigPipe")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
    }
  }
}
