package actor.circuitbreaker

import akka.actor._
import akka.pattern.CircuitBreaker

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

case class Message[T](c: T)

class CircuitBreakerActor[T](f: T => Any) extends Actor with ActorLogging {

  val breaker =
    new CircuitBreaker(context.system.scheduler,
      maxFailures = 1,
      callTimeout = 5.seconds,
      resetTimeout = 5.seconds).onOpen(notifyMeOnOpen())
      .onClose(notifyMeOnClose())

  def notifyMeOnOpen(): Unit =
    log.warning("My CircuitBreaker is now open, and will not close for 5 seconds")

  def notifyMeOnClose(): Unit =
    log.warning("My CircuitBreaker is now close")

  def receive = {
    case s: Message[T] =>
      sender() ! breaker.withCircuitBreaker(Future(f(s.c)))
  }
}


