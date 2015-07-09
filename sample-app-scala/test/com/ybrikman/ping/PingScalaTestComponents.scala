package com.ybrikman.ping

import loader.PingComponents
import play.api.routing.Router
import play.api.{Mode, Environment, ApplicationLoader}

trait PingScalaTestComponents extends PingTestComponentsProvider {

  override def createTestComponents(customRoutes: Option[(RouterComponents) => Router]): PingTestComponents = {
    val components = new PingComponentsForTest(createContext, customRoutes)
    PingTestComponents(components.application, components.wsClient)
  }

  private def createContext: ApplicationLoader.Context = {
    val env = new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test)
    ApplicationLoader.createContext(env)
  }
}

class PingComponentsForTest(context: ApplicationLoader.Context, customRoutes: Option[(RouterComponents) => Router]) extends PingComponents(context) {
  override val router: Router = customRoutes.map(f => f(RouterComponents(actorSystem))).getOrElse(routes)
}
