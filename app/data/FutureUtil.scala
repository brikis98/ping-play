package data

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import akka.pattern.after

class FutureUtil(actorSystem: ActorSystem) {
  def timeout[A](message: => A, delay: Long, unit: TimeUnit = TimeUnit.MILLISECONDS)(implicit ec: ExecutionContext): Future[A] = {
    after(FiniteDuration(delay, TimeUnit.MILLISECONDS), actorSystem.scheduler)(Future(message))
  }
}
