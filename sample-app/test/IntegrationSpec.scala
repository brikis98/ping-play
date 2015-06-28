import loader.PingApplicationLoader
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.{Mode, Environment, ApplicationLoader, Application}

import play.api.test._

import controllers.Mock

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  // Without this, the WithBrowser helper uses Guice to load your app
  def app: Application = {
    val context = ApplicationLoader.createContext(new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test))
    new PingApplicationLoader().load(context)
  }

  "Application" should {

    "render the non-streaming WVYP page" in new WithBrowser(app = app) {
      browser.goTo(s"http://localhost:$port/wvyp")
      browser.$(".wvyp-count .large-number").getTexts.get(0) must equalTo(Mock.DEFAULT_WVYP_RESPONSE)
    }

    "render the streaming WVYP page" in new WithBrowser(app = app) {
      browser.goTo(s"http://localhost:$port/stream")
      browser.$(".wvyp-count .large-number").getTexts.get(0) must equalTo(Mock.DEFAULT_WVYP_RESPONSE)
    }
  }
}
