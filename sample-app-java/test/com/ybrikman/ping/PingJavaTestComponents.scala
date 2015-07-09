package com.ybrikman.ping

import akka.actor.ActorSystem
import play.api.libs.ws.WSClient
import play.api.mvc.{RequestHeader, Handler}
import play.api.routing.Router
import play.api.test.{FakeRequest, FakeApplication}

trait PingJavaTestComponents extends PingTestComponentsProvider {
  override def createTestComponents(customRoutes: Option[(RouterComponents) => Router]): PingTestComponents = {
    val initialApp = FakeApplication()
    val actorSystem = initialApp.injector.instanceOf(classOf[ActorSystem])
    val wsClient = initialApp.injector.instanceOf(classOf[WSClient])

    val routes = customRoutes
      .map(f => f(RouterComponents(actorSystem)))
      .map(routerToPartialFunction)
      .getOrElse(PartialFunction.empty)

    val app = initialApp.copy(withRoutes = routes)

    PingTestComponents(app, wsClient)
  }

  private def routerToPartialFunction(router: Router): PartialFunction[(String, String), Handler] = {
    val toRequestHeader: PartialFunction[(String, String), RequestHeader] = {
      case (method, path) => FakeRequest(method, path)
    }

    toRequestHeader.andThen(router.routes)
  }
}
