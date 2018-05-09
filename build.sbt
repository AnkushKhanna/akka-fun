name := "akka-fun"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "confluent" at "http://packages.confluent.io/maven/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.lightbend" %% "kafka-streams-scala" % "0.2.1",
  "org.apache.kafka" % "kafka-streams" % "1.1.0",
  "io.confluent" % "kafka-connect-avro-converter" % "3.2.1",
  "io.confluent" % "kafka-streams-avro-serde" % "3.3.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.3",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.8",
  "org.mockito" % "mockito-all" % "1.8.4" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test",
  "org.apache.kafka" % "kafka-streams-test-utils" % "1.1.0" % "test"
)

