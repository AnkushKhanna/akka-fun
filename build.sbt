name := "akka-fun"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.7",
  "org.apache.kafka" % "kafka-clients" % "0.9.0.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.3",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "org.mockito" % "mockito-all" % "1.8.4" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.2" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
)
