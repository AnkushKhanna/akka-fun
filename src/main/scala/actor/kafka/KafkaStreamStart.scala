package actor.kafka

import java.util.Properties

import akka.actor.{ActorSystem, Props}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.StreamsConfig

object KafkaStreamStart extends App {

  private val inputTopic = "input-topic"
  private val outputTopic = "output-topic"
  private val bootstrapServer = "127.0.0.1:9092"

  private val actorSystem = ActorSystem("KafkaStream-Actor-start")

  val kafkaStreamActor = actorSystem.actorOf(Props(classOf[KafkaStreamActor],
    new SimpleTopology(inputTopic, outputTopic), configuration()),
    "kafka-stream-test")

  sys.addShutdownHook(() =>
    actorSystem.terminate()
  )

  private def configuration() = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "testing-kafka-stream-start")
    p.put(StreamsConfig.CLIENT_ID_CONFIG, "testing-kafka-stream-start-client-id")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
    p.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "1")

    p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, classOf[StringSerde])
    p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, classOf[StringSerde])
    p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

    p
  }
}
