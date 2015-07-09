package loader

import com.ybrikman.ping.scalaapi.dedupe.{CacheFilter, DedupingCache}
import controllers._
import data.{ServiceClient, FutureUtil, FakeServiceClient}
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ning.NingWSComponents
import play.api.routing.Router
import play.api.{BuiltInComponentsFromContext, Application, ApplicationLoader}
import play.api.ApplicationLoader.Context
import router.Routes
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

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
  val cache = new DedupingCache[String, Future[WSResponse]]
  val futureUtil = new FutureUtil(actorSystem)

  val fakeServiceClient = new FakeServiceClient(futureUtil)
  val serviceClient = new ServiceClient(wsClient, cache)

  val withoutBigPipe = new WithoutBigPipe(fakeServiceClient)
  val withBigPipe = new WithBigPipe(fakeServiceClient)
  val moreBigPipeExamples = new MoreBigPipeExamples(fakeServiceClient)

  val deduping = new Deduping(serviceClient)
  val mock = new Mock

  val assets = new Assets(httpErrorHandler)

  val routes =  new Routes(
    httpErrorHandler,
    withoutBigPipe,
    withBigPipe,
    moreBigPipeExamples,
    deduping,
    mock,
    assets)

  override val router: Router = routes

  val cacheFilter = new CacheFilter(cache)
  override lazy val httpFilters = Seq(cacheFilter)
}
