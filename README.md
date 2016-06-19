# akka-fun

CongregateMessages actor, fsm.
Lets you congregate/aggregate messages and publish it back
to the passed function.
The call back is done when either the number of messages are
exceeded a threshold or some time has elapsed, specified by
Rate case class.
