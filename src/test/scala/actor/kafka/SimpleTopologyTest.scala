package actor.kafka

import java.util.Properties

import com.lightbend.kafka.scala.streams.StreamsBuilderS
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.OutputVerifier
import org.scalatest.FlatSpec

import org.apache.kafka.streams.test.ConsumerRecordFactory
import scala.collection.JavaConverters._

class SimpleTopologyTest extends FlatSpec {

  "Topology" should "just pass records to output topic" in {
    import org.apache.kafka.streams.StreamsConfig
    val config = new Properties()
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test")
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234")

    val builder = new StreamsBuilderS()

    val kafkaStreamTopology = new SimpleTopology("input-topic", "output-topic")

    val testDriver = new TopologyTestDriver(kafkaStreamTopology.defineTopology(builder), config)

    val factory = new ConsumerRecordFactory[String, String]("input-topic", new StringSerializer(), new StringSerializer())

    testDriver.pipeInput(List(factory.create("input-topic", "key", "42")).asJava)
    val outputRecord = testDriver.readOutput("output-topic", new StringDeserializer(), new StringDeserializer())
    OutputVerifier.compareKeyValue(outputRecord, "key", "42")

  }
}
