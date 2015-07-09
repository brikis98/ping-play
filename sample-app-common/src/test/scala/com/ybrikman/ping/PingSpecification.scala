package com.ybrikman.ping

import akka.actor.ActorSystem
import org.specs2.execute.{AsResult, Result}
import play.api.Application
import play.api.libs.ws.WSClient
import play.api.routing.Router
import play.api.test.{PlaySpecification, DefaultAwaitTimeout, FutureAwaits, WithServer}

trait PingSpecification extends PlaySpecification with PingTestComponentsProvider

trait PingTestComponentsProvider {
  def createTestComponents(customRoutes: Option[RouterComponents => Router] = None): PingTestComponents
}

/**
 * Common class that abstracts away whether the underlying app uses Java or Scala and how it's initialized.
 *
 * @param app
 * @param wsClient
 */
case class PingTestComponents(app: Application, wsClient: WSClient)

/**
 * A bit of an ugly hack. Some test cases need custom routing. One in particular has a custom action that depends on
 * access to the ActorSystem. I can't figure out an easy way to solve this that works with both run-time dependency
 * injection (for Java apps) and compile-time dependency injection (for Scala apps), so this is an ugly workaround.
 *
 * @param actorSystem
 */
case class RouterComponents(actorSystem: ActorSystem)

/**
 * An extension of specs2 "Around" that can be used to fire up a test server in one line.
 *
 * @param components
 */
abstract class WithPingTestServer(val components: PingTestComponents) extends WithServer(app = components.app)

/**
 * Same as WithPingTestServer, except this one hits the /warmup URL a bunch of times before running the test. This is
 * useful to ensure the app is fully up and running so that tests sensitive to timing are not thrown off by bootup
 * and initialization routines.
 *
 * @param components
 */
abstract class WithWarmedUpPingTestServer(components: PingTestComponents) extends WithPingTestServer(components) with FutureAwaits with DefaultAwaitTimeout {
  override def around[T](t: => T)(implicit evidence$3: AsResult[T]): Result = {
    super.around {
      warmup()
      t
    }
  }

  // Make sure the server is warmed up so our timing is not thrown off by bootup and initialization routines
  private def warmup(): Unit = {
    (0 until 15).foreach { _ =>
      await(components.wsClient.url(s"http://localhost:$port/warmup").get())
    }
  }
}