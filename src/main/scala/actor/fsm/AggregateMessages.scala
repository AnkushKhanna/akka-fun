package actor.fsm

import akka.actor.{Actor, FSM, Props}

import scala.collection.immutable.{Queue => Q}
import scala.concurrent.duration.FiniteDuration

sealed trait State

case object Idle extends State

case object Collect extends State

case object Caller extends State

case class Message[T](value: T)

case object Apply

case object HardTimeOut

sealed case class Data[T](queue: Q[Message[T]])

case class Rate(noOfMessage: Int, time: FiniteDuration)

/**
  * Aggregate Message, has 3 stages:
  * Ideal: does nothing
  * Collect: excepts Message and store it in a queue.
  * Caller: calls the function passed as parameter, with the collected message.
  *
  * Transitions:
  * Ideal -> Collect : When a message is received.
  * Collect -> Caller : When either the time specified in Rate is expired
  * or the number of message collected are greater than noOfMessages specified in Rate.
  * Caller -> Ideal : After calling the function.
  *
  **/
class AggregateMessages[T, E](rate: Rate, conversion: List[T] => E) extends Actor with FSM[State, Data[T]] {

  startWith(Idle, Data(Q[Message[T]]()))

  when(Idle) {
    case Event(m: Message[T], d: Data[T]) =>
      goto(Collect) using d.copy(queue = d.queue.enqueue(m))
  }

  when(Collect, stateTimeout = rate.time) {
    case Event(m: Message[T], d: Data[T]) =>
      val queue = d.queue.enqueue(m)
      if (queue.size >= rate.noOfMessage) {
        goto(Caller) using d.copy(queue = queue)
      } else {
        stay() using d.copy(queue = queue)
      }
    case Event(StateTimeout, d: Data[T]) =>
      goto(Caller)
    case Event(HardTimeOut, d: Data[T]) =>
      goto(Caller)
  }

  when(Caller) {
    case Event(Apply, d: Data[T]) =>
      call(d)
      goto(Idle) using d.copy(queue = d.queue.drop(rate.noOfMessage))
    case Event(m: Message[T], d: Data[T]) =>
      call(d)
      goto(Collect) using d.copy(queue = d.queue.drop(rate.noOfMessage).enqueue(m))
  }

  onTransition {
    case Collect -> Caller => self ! Apply
    case Idle -> Collect => setTimer("hard-time-out", HardTimeOut, rate.time, repeat = false)
  }

  onTermination {
    case StopEvent(FSM.Failure(cause), _, d: Data[T]) =>
      log.error("FSM Stop because of error", cause)
      call(d)
    case StopEvent(_, _, d: Data[T]) =>
      call(d)
  }

  initialize()

  private def call(d: Data[T]) = {
    context.parent ! conversion(d.queue.take(rate.noOfMessage).map(_.value).toList)
  }
}

object AggregateMessages {
  def props[T, E](rate: Rate, conversion: List[T] => E) = Props(new AggregateMessages(rate, conversion))
}