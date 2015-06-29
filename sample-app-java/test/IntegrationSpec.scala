import org.specs2.mutable.Specification
import play.api.test.WithBrowser
import scala.collection.JavaConverters._

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

    "dedupe remote calls" in new WithBrowser {
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
