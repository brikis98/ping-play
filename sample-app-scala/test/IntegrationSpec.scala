import loader.PingApplicationLoader
import org.specs2.mutable._
import play.api.test._
import play.api.{Application, ApplicationLoader, Environment, Mode}

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
  }
}
