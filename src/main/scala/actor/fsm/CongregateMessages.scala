package actor.fsm

import akka.actor.{Actor, FSM}

import scala.collection.immutable.{Queue => Q}
import scala.concurrent.duration.FiniteDuration

sealed trait State

case object Idle extends State

case object Collect extends State

case object Caller extends State

case class Message[T](message: T)

case class Apply()

sealed case class Data[T](queue: Q[Message[T]])

case class Rate(noOfMessage: Int, time: FiniteDuration)

class CongregateMessages[T](f: List[T] => Unit, rate: Rate) extends Actor with FSM[State, Data[T]] {

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
  }

  when(Caller) {
    case Event(a: Apply, d: Data[T]) =>
      f.apply(d.queue.take(rate.noOfMessage).map(_.message).toList)
      goto(Idle) using d.copy(queue = d.queue.drop(rate.noOfMessage))
    case Event(m: Message[T], d: Data[T]) =>
      stay() using d.copy(queue = d.queue.enqueue(m))
  }

  onTransition {
    case Collect -> Caller ⇒ self ! Apply()
  }

  initialize()
}
