import loader.PingApplicationLoader
import org.specs2.mutable._
import play.api.test._
import play.api.{Application, ApplicationLoader, Environment, Mode}
import scala.collection.JavaConverters._

class IntegrationSpec extends Specification {

  // Without this, the WithBrowser helper uses Guice to load your app
  def app: Application = {
    val context = ApplicationLoader.createContext(new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test))
    new PingApplicationLoader().load(context)
  }

  "Application" should {
    "render page without big pipe" in new WithBrowser(app = app) {
      browser.goTo(s"http://localhost:$port/withoutBigPipe")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
    }

    "render the streaming WVYP page" in new WithBrowser(app = app) {
      browser.goTo(s"http://localhost:$port/withBigPipe")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
    }

    "dedupe remote calls" in new WithBrowser(app = app) {
      browser.goTo(s"http://localhost:$port/dedupe")
      val values = browser.$(".id").getTexts.asScala

      // First 3 values should be the same since they were de-duped, fourth should be different
      values must have size 4
      values(0) mustEqual values(1)
      values(1) mustEqual values(2)
      values(1) mustNotEqual values(3)
    }

    "be able to render Mustache.js templates client-side" in new WithBrowser(app = app) {
      browser.goTo(s"http://localhost:$port/clientSideTemplating")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
    }

    "be able to handle errors" in new WithBrowser(app = app) {
      browser.goTo(s"http://localhost:$port/errorHandling")
      browser.$("#profile .id").getTexts.get(0) must equalTo("profile")
      browser.$("#feed .id").getTexts.get(0) must equalTo("error")
    }
  }
}
