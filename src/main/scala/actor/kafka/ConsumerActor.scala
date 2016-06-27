package actor.kafka

import java.util.Properties

import akka.actor.{Actor, ActorSystem, Props}
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


class ConsumerActor[V](c: Class[V], topics: List[String], groupId: String, f: List[V] => Any, properties: Map[String, String]) extends Actor {
  private val consumer = new Consumer[V]()

  import ConsumerActor._

  @throws[Exception](classOf[Exception])
  override def preStart() = {
    consumer.start(topics, groupId, properties)
    context.system.scheduler.scheduleOnce(5 seconds, self, Consume)
  }

  override def receive = {
    case Consume =>
      val messages: List[V] = consumer.consume(c)
      f(messages)
      context.system.scheduler.scheduleOnce(5 seconds, self, Consume)
  }
}

object ConsumerActor {

  case object Consume

}

private class Consumer[V] {

  private val mapper = new Mapper()
  private var kafkaConsumer: KafkaConsumer[String, String] = _

  def start(topics: List[String], groupId: String, properties: Map[String, String]) = {
    val prop: Properties = new Properties()
    prop.putAll(properties.asJava)

    kafkaConsumer = new KafkaConsumer(prop)
    kafkaConsumer.subscribe(topics.asJava)
  }

  def consume(c: Class[V]): List[V] = {
    def convert(records: List[ConsumerRecord[String, String]], c: Class[V]): List[V] = {
      for {
        record <- records
        message = mapper.readValue(record.value(), c)
      } yield message
    }

    val records = kafkaConsumer.poll(500).iterator().asScala.toList
    convert(records, c)
  }
}

private class Mapper extends ObjectMapper with ScalaObjectMapper {
  this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
  this.registerModule(DefaultScalaModule)
}

class ConsumerKafka(properties: Map[String, String]) {

  def map[V](c: Class[V], topics: List[String], groupId: String)(f: List[V] => Any)(implicit actorSystem: ActorSystem) = {
    actorSystem.actorOf(Props.apply(new ConsumerActor[V](c, topics, groupId, f, properties)))
  }
}

