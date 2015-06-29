package loader

import controllers._
import data.{FutureUtil, ServiceClient}
import play.api.libs.ws.ning.NingWSComponents
import play.api.routing.Router
import play.api.{BuiltInComponentsFromContext, Application, ApplicationLoader}
import play.api.ApplicationLoader.Context
import router.Routes

/**
 * This is the entry point for loading this Play app, as configured in conf/application.conf.
 */
class PingApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    new PingComponents(context).application
  }
}

/**
 * Compile-time dependency injection for this Play app.
 *
 * @param context
 */
class PingComponents(context: Context) extends BuiltInComponentsFromContext(context) with NingWSComponents {
  lazy val futureUtil = new FutureUtil(actorSystem)
  lazy val serviceClient = new ServiceClient(futureUtil)

  lazy val withoutBigPipe = new WithoutBigPipe(serviceClient)
  lazy val withBigPipe = new WithBigPipe(serviceClient)
  lazy val assets = new Assets(httpErrorHandler)

  lazy val router: Router = new Routes(
    httpErrorHandler,
    withoutBigPipe,
    withBigPipe,
    assets)
}
