package loader

import controllers._
import data.{FutureUtil, ServiceClientJ, ServiceClient}
import play.api.libs.ws.ning.NingWSComponents
import play.api.routing.Router
import play.api.{BuiltInComponentsFromContext, Application, ApplicationLoader}
import play.api.ApplicationLoader.Context
import play.inject.DelegateApplicationLifecycle
import play.libs.ws.ning.NingWSAPI
import router.Routes

class PingApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    new PingComponents(context).application
  }
}

class PingComponents(context: Context) extends BuiltInComponentsFromContext(context) with NingWSComponents {
  lazy val serviceClient = new ServiceClient(wsClient)
  lazy val serviceClientJ = new ServiceClientJ(new NingWSAPI(ningWsClientConfig, new DelegateApplicationLifecycle(applicationLifecycle)).client())

  lazy val futureUtil = new FutureUtil(actorSystem)

  lazy val enumeratorExamples = new EnumeratorExamples(futureUtil)
  lazy val mock = new Mock(futureUtil)

  lazy val wvyp = new Wvyp(serviceClient)
  lazy val wvyu = new Wvyu(serviceClient)

  lazy val wvypEnumerator = new WvypEnumerator(serviceClient)
  lazy val aggregator = new Aggregator(wvyp, wvyu)

  lazy val wvypStream = new WvypStream(serviceClient)
  lazy val wvypStreamJava = new WvypStreamJava(serviceClientJ)
  lazy val assets = new Assets(httpErrorHandler)

  lazy val router: Router = new Routes(
    httpErrorHandler,
    mock,
    wvyu,
    wvyp,
    aggregator,
    wvypStream,
    wvypStreamJava,
    enumeratorExamples,
    wvypEnumerator,
    assets)
}
