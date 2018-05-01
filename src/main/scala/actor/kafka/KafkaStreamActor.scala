package actor.kafka

import java.util.Properties

import actor.kafka.KafkaStreamActor.{Restart, Start, Stop}
import akka.actor.{Actor, ActorLogging}
import com.lightbend.kafka.scala.streams.StreamsBuilderS
import org.apache.kafka.streams.KafkaStreams

class KafkaStreamActor(kafkaStreamTopology: KafkaStreamTopology, properties: Properties) extends Actor with ActorLogging {
  private var streams: KafkaStreams = _

  private def start() = {

    val builder = new StreamsBuilderS()

    val topology = kafkaStreamTopology.defineTopology(builder)

    streams = new KafkaStreams(topology, properties)

    streams.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
      override def uncaughtException(t: Thread, e: Throwable): Unit = {
        log.error("Restarted Stream due to", e)
        self ! Restart
      }
    })

    streams.start()
  }

  override def receive: Receive = {
    case Start => start()
    case Restart =>
      streams.close()
      start()
    case Stop =>
      streams.close()
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

  case object Restart

  case object Stop

  case object StreamMetrics

}
