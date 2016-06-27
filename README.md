# akka-fun

#### AggregateMessages actor, fsm:
Lets you congregate/aggregate messages and publish it back
to the passed function.

The call back is done when either the number of messages are
exceeded a threshold or some time has elapsed, specified by "Rate" case class.

####  ConsumerKafka actor, kafka:
Lets you open a consumer in a different actor, polling kafka every 5 seconds.

Calling the function with the result consumed from polling.

Also support mapping from String to Object.
