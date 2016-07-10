package actor.persistence

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import akka.persistence.{AtLeastOnceDelivery, PersistentActor}
/**
 * This all is just a very simple trying of
 * AtLeastOnceDelivery trait.
 * To be honest there is nothing special here
 * except that you actually see it working.
 * */
case class Msg(deliveryId: Long, s: String)

case class Confirm(deliveryId: Long)

sealed trait Evt

case class MsgSent(s: String) extends Evt

case class MsgConfirmed(deliveryId: Long) extends Evt

class AtLeastOnceDeliveryActor(destination: ActorSelection)
  extends PersistentActor with AtLeastOnceDelivery {

  override def persistenceId: String = "persistence-id"

  override def receiveCommand: Receive = {
    case s: String => persist(MsgSent(s))(updateState)
    case Confirm(deliveryId) => persist(MsgConfirmed(deliveryId))(updateState)
  }

  override def receiveRecover: Receive = {
    case evt: Evt => updateState(evt)
  }

  def updateState(evt: Evt): Unit = evt match {
    case MsgSent(s) =>
      deliver(destination)(deliveryId => Msg(deliveryId, s))

    case MsgConfirmed(deliveryId) => confirmDelivery(deliveryId)
  }
}

class MyDestination extends Actor {
  def receive = {
    case Msg(deliveryId, s) =>
      println(s, deliveryId)
      if(s != "B") sender() ! Confirm(deliveryId)
  }
}

object Start {
  def main(args: Array[String]) {
    val actorSystem = ActorSystem.create()
    val destination = actorSystem.actorSelection(actorSystem.actorOf(Props.apply(classOf[MyDestination]), "destination").path)
    val atLeastOnceDelivery = actorSystem.actorOf(Props.apply(classOf[AtLeastOnceDeliveryActor], destination))
    atLeastOnceDelivery ! "A"
    atLeastOnceDelivery ! "B"
  }
}
