package actor.kafka

import akka.actor.{ActorRef, ActorSystem, Props}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.duration._
import scala.language.postfixOps

case class KafkaTest(name: String)

class ConsumerKafkaTest extends FlatSpec with MockitoSugar {

  implicit val actor = mock[ActorSystem]
  implicit val duration: FiniteDuration = 5 second
  val consumerKafka = new ConsumerKafka[KafkaTest](Map.empty, List("topic"), "groupId")

  "Test" should "just check if the consumerKafka map works" in {
    val ar = mock[ActorRef]
    when(actor.actorOf(any(classOf[Props]))).thenReturn(ar)
    consumerKafka.map {
      kafkaTests =>
        kafkaTests.size
    }
  }


}
