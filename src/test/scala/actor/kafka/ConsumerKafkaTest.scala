package actor.kafka

import akka.actor.{ActorRef, ActorSystem, Props}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar

case class KafkaTest(name: String)

class ConsumerKafkaTest extends FlatSpec with MockitoSugar {

  val consumerKafka = new ConsumerKafka[KafkaTest](Map.empty, List("topic"), "groupId")
  implicit val actor = mock[ActorSystem]

  "Test" should "blah blah" in {
    val ar = mock[ActorRef]
    when(actor.actorOf(any(classOf[Props]))).thenReturn(ar)
    consumerKafka.map {
      kafkaTests => println(kafkaTests.size)
    }
  }


}
