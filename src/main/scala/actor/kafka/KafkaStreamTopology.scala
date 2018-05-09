package actor.kafka

import com.lightbend.kafka.scala.streams.StreamsBuilderS
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.{Consumed, Topology}

trait KafkaStreamTopology {

  def defineTopology(builder: StreamsBuilderS): Topology
}

class SimpleTopology(inputTopic: String, outputTopic: String) extends KafkaStreamTopology {

  import com.lightbend.kafka.scala.streams.DefaultSerdes._

  implicit val consumed: Consumed[String, String] = Consumed.`with`(stringSerde, stringSerde)
  implicit val produced: Produced[String, String] = Produced.`with`(stringSerde, stringSerde)

  override def defineTopology(builder: StreamsBuilderS): Topology = {
    builder.stream[String, String](inputTopic)
      .to(outputTopic)
    builder.build()
  }
}
