package actor.kafka

import java.util.Properties

import actor.kafka.KafkaStreamActor.Start
import akka.actor.{Actor, ActorLogging}
import com.lightbend.kafka.scala.streams.StreamsBuilderS
import org.apache.kafka.streams.KafkaStreams

/**
  * Start Kafka Streams in a reliable way. In case of
  * Exception in stream, the supervisor strategy would try a restart.
  * In case of specific exception handling, should create SupervisorStrategyConfigurator
  * and define it in akka.actor.guardian-supervisor-strategy.
  *
  * @param kafkaStreamTopology defines the topology for the stream
  * @param properties define the properties stream have to start with
  * */
class KafkaStreamActor(kafkaStreamTopology: KafkaStreamTopology, properties: Properties) extends Actor with ActorLogging {
  private var streams: KafkaStreams = _

  private def start() = {

    val builder = new StreamsBuilderS()

    val topology = kafkaStreamTopology.defineTopology(builder)

    streams = new KafkaStreams(topology, properties)

    streams.start()
  }

  override def receive: Receive = {
    case Start => start()
  }

  override def preStart(): Unit = {
    super.preStart()
    log.info("Pre Start of Streaming Actor.")
    self ! Start
  }

  override def postStop(): Unit = {
    super.postStop()
    log.info("Closing kafka Stream actor")
    streams.close()
  }
}

object KafkaStreamActor {

  case object Start
}
