package actor.fsm

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec}

import scala.concurrent.duration._
import scala.language.postfixOps

class CongregateMessagesTest extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach with MockitoSugar {

  implicit val actorSystem = ActorSystem.apply("CongregateMessageTest")
  var toBeCalled: ToBeCalled = _
  var prob: TestProbe = _

  case class UpdateMessage(id: Long)

  class ToBeCalled(actor: ActorRef) {
    def caller(list: List[UpdateMessage]) = {
      actor ! list
    }
  }

  override protected def beforeEach() = {
    prob = TestProbe()
    toBeCalled = new ToBeCalled(prob.ref)
  }

  override protected def afterAll() = {
    actorSystem.terminate()
  }

  "CongregateMessagesActor" should
    "congregate messages and call function with List, when time expires" in {
    val actor = TestActorRef.apply(new CongregateMessages[UpdateMessage](toBeCalled.caller, Rate(100, 1 second)))

    actor ! Message(UpdateMessage(100))
    actor ! Message(UpdateMessage(200))
    actor ! Message(UpdateMessage(300))

    prob.expectMsg(List(UpdateMessage(100), UpdateMessage(200), UpdateMessage(300)))

  }

  it should
    "congregate messages and call function when no of max messages exceeds" in {
    val actor = TestActorRef.apply(new CongregateMessages[UpdateMessage](toBeCalled.caller, Rate(3, 10 second)))

    actor ! Message(UpdateMessage(10))
    actor ! Message(UpdateMessage(20))
    actor ! Message(UpdateMessage(30))
    actor ! Message(UpdateMessage(40))

    prob.expectMsg(List(UpdateMessage(10), UpdateMessage(20), UpdateMessage(30)))
  }

  it should
    "congregate messages and call function multiple times, " +
      "if message is > 2*noOfMessage" in {
    val actor = TestActorRef.apply(new CongregateMessages[UpdateMessage](toBeCalled.caller, Rate(3, 10 second)))

    actor ! Message(UpdateMessage(10))
    actor ! Message(UpdateMessage(20))
    actor ! Message(UpdateMessage(30))
    actor ! Message(UpdateMessage(40))
    actor ! Message(UpdateMessage(50))
    actor ! Message(UpdateMessage(60))

    prob.expectMsg(List(UpdateMessage(10), UpdateMessage(20), UpdateMessage(30)))
    prob.expectMsg(List(UpdateMessage(40), UpdateMessage(50), UpdateMessage(60)))
    prob.expectNoMsg()
  }

  it should
    "congregate messages and call function multiple times," +
      "if message is < 2*noOfMessage, " +
      "after time expires" in {
    val actor = TestActorRef.apply(new CongregateMessages[UpdateMessage](toBeCalled.caller, Rate(3, 1 second)))

    actor ! Message(UpdateMessage(10))
    actor ! Message(UpdateMessage(20))
    actor ! Message(UpdateMessage(30))
    actor ! Message(UpdateMessage(40))
    actor ! Message(UpdateMessage(50))

    prob.expectMsg(List(UpdateMessage(10), UpdateMessage(20), UpdateMessage(30)))
    prob.expectMsg(2 second, List(UpdateMessage(40), UpdateMessage(50)))
  }

  it should
    "congregate messages and call function multiple times," +
      "event if messages are send at different time " +
      "after time expires" in {
    val actor = TestActorRef.apply(new CongregateMessages[UpdateMessage](toBeCalled.caller, Rate(3, 1 second)))

    actor ! Message(UpdateMessage(10))
    actor ! Message(UpdateMessage(20))

    prob.expectMsg(2 second, List(UpdateMessage(10), UpdateMessage(20)))

    actor ! Message(UpdateMessage(110))
    actor ! Message(UpdateMessage(220))
    actor ! Message(UpdateMessage(330))

    prob.expectMsg(List(UpdateMessage(110), UpdateMessage(220), UpdateMessage(330)))
  }
}
