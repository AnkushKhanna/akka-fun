# akka-fun

#### AggregateMessages actor, fsm:
Lets you congregate/aggregate messages and publish it back
to the passed function.

The callback is done when either the number of messages
exceeds a threshold or time has elapsed,
specified by "Rate" case class.

####  ConsumerKafka actor, kafka:
Lets you open a consumer in a different actor, polling every 5 seconds.
Function passed to map would be called with the result from polling.

Support mapping from Json to Object.
Assuming: value.serializer is specified as "org.apache.kafka.common.serialization.StringSerializer"

```scala
case class MyComponent(name: String)

val topicNames: List[String] = List("Topic-to-consume-from")

val groupId = "group-id"

val kafkaConsumerProperties = new java.util.Properties()
kafkaConsumerProperties.putAll(...)

val consumer = new ConsumerKafka[MyComponent](kafkaConsumerProperties, topicNames, groupId)

consumer.map {
  components: List[MyComponent] => 
    components.map(...)
}
```

#### AtLeastOnceDelivery, Persistent Actor:
Creates a Persistent actor which wraps an actor on which the message should
be delieverd at-least once.
