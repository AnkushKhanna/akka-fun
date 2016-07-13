# akka-fun

#### AggregateMessages actor, fsm:
Lets you congregate/aggregate messages and publish it back
to the passed function.

The call back is done when either the number of messages are
exceeded a threshold or some time has elapsed, specified by "Rate" case class.

####  ConsumerKafka actor, kafka:
Lets you open a consumer in a different actor, polling kafka every 5 seconds.
Function passed to map would be called with the result from polling.

Also support mapping from Json to Object.

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
