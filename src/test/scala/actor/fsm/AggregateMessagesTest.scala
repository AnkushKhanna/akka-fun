package actor.fsm

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec}

import scala.concurrent.duration._
import scala.language.postfixOps

class AggregateMessagesTest extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  private implicit val actorSystem = ActorSystem.apply("CongregateMessageTest")
  private var prob: TestProbe = _

  case class UpdateMessage(id: Long)

  case class ExpectedMessage(messages: List[UpdateMessage])

  override protected def beforeEach() = {
    prob = TestProbe()
  }

  override protected def afterAll() = {
    actorSystem.terminate()
  }

  "CongregateMessagesActor" should
    "congregate messages and call function with List, when time expires" in {
    val actor = prob.childActorOf(AggregateMessages.props[UpdateMessage, ExpectedMessage](Rate(100, 1 second), messages => ExpectedMessage(messages)))

    actor ! Message(UpdateMessage(100))
    actor ! Message(UpdateMessage(200))
    actor ! Message(UpdateMessage(300))

    prob.expectMsg(ExpectedMessage(List(UpdateMessage(100), UpdateMessage(200), UpdateMessage(300))))

  }

  it should
    "congregate messages and call function when no of max messages exceeds" in {
    val actor = prob.childActorOf(AggregateMessages.props[UpdateMessage, ExpectedMessage](Rate(3, 10 second), messages => ExpectedMessage(messages)))

    actor ! Message(UpdateMessage(10))
    actor ! Message(UpdateMessage(20))
    actor ! Message(UpdateMessage(30))
    actor ! Message(UpdateMessage(40))

    prob.expectMsg(ExpectedMessage(List(UpdateMessage(10), UpdateMessage(20), UpdateMessage(30))))
  }

  it should
    "congregate messages and call function multiple times, " +
      "if message is > 2*noOfMessage" in {
    val actor = prob.childActorOf(AggregateMessages.props[UpdateMessage, ExpectedMessage](Rate(3, 10 second), messages => ExpectedMessage(messages)))

    actor ! Message(UpdateMessage(10))
    actor ! Message(UpdateMessage(20))
    actor ! Message(UpdateMessage(30))
    actor ! Message(UpdateMessage(40))
    actor ! Message(UpdateMessage(50))
    actor ! Message(UpdateMessage(60))

    prob.expectMsg(ExpectedMessage(List(UpdateMessage(10), UpdateMessage(20), UpdateMessage(30))))
    prob.expectMsg(ExpectedMessage(List(UpdateMessage(40), UpdateMessage(50), UpdateMessage(60))))
    prob.expectNoMessage()
  }

  it should
    "congregate messages and call function multiple times," +
      "if message is < 2*noOfMessage, " +
      "after time expires" in {
    val actor = prob.childActorOf(AggregateMessages.props[UpdateMessage, ExpectedMessage](Rate(3, 1 second), messages => ExpectedMessage(messages)))

    actor ! Message(UpdateMessage(10))
    actor ! Message(UpdateMessage(20))
    actor ! Message(UpdateMessage(30))
    actor ! Message(UpdateMessage(40))
    actor ! Message(UpdateMessage(50))

    prob.expectMsg(ExpectedMessage(List(UpdateMessage(10), UpdateMessage(20), UpdateMessage(30))))
    prob.expectMsg(2 second, ExpectedMessage(List(UpdateMessage(40), UpdateMessage(50))))
  }

  it should
    "congregate messages and call function multiple times," +
      "event if messages are send at different time " +
      "after time expires" in {
    val actor = prob.childActorOf(AggregateMessages.props[UpdateMessage, ExpectedMessage](Rate(3, 1 second), messages => ExpectedMessage(messages)))

    actor ! Message(UpdateMessage(10))
    actor ! Message(UpdateMessage(20))

    prob.expectMsg(2 second, ExpectedMessage(List(UpdateMessage(10), UpdateMessage(20))))

    actor ! Message(UpdateMessage(110))
    actor ! Message(UpdateMessage(220))
    actor ! Message(UpdateMessage(330))

    prob.expectMsg(ExpectedMessage(List(UpdateMessage(110), UpdateMessage(220), UpdateMessage(330))))
  }

  it should
    "send messages to prob even if it receives continues messages because of timeOutContinues" in {
    val actor = prob.childActorOf(AggregateMessages.props[UpdateMessage, ExpectedMessage](Rate(4, 3 second), messages => ExpectedMessage(messages)))

    actor ! Message(UpdateMessage(10))
    Thread.sleep(900)
    actor ! Message(UpdateMessage(20))
    Thread.sleep(900)
    actor ! Message(UpdateMessage(30))
    Thread.sleep(900)
    prob.expectMsg(2 second, ExpectedMessage(List(UpdateMessage(10), UpdateMessage(20), UpdateMessage(30))))
  }
}
