package data

import java.util.concurrent.TimeUnit
import java.util.function.Supplier

import akka.actor.ActorSystem
import play.libs.F.Promise
import play.libs.HttpExecution

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import akka.pattern.after

class FutureUtil(actorSystem: ActorSystem) {

  /**
   * Return a Scala Future that will be redeemed with the given message after the specified delay.
   *
   * @param message
   * @param delay
   * @param unit
   * @param ec
   * @tparam A
   * @return
   */
  def timeout[A](message: => A, delay: Long, unit: TimeUnit = TimeUnit.MILLISECONDS)(implicit ec: ExecutionContext): Future[A] = {
    after(FiniteDuration(delay, TimeUnit.MILLISECONDS), actorSystem.scheduler)(Future(message))
  }

  /**
   * Return a Java Promise that will be redeemed with the given message after the specified delay.
   *
   * @param message
   * @param delay
   * @param unit
   * @tparam A
   * @return
   */
  def timeout[A](message: Supplier[A], delay: Long, unit: TimeUnit): Promise[A] = {
    timeout(message, delay, unit, HttpExecution.defaultContext())
  }

  /**
   * Return a Java Promise that will be redeemed with the given message after the specified delay.
   *
   * @param message
   * @param delay
   * @param unit
   * @param ec
   * @tparam A
   * @return
   */
  def timeout[A](message: Supplier[A], delay: Long, unit: TimeUnit, ec: ExecutionContext): Promise[A] = {
    Promise.wrap(timeout(message.get(), delay)(ec))
  }
}
