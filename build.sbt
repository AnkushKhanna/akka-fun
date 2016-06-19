name := "akka-fun"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % "2.4.7",
  "org.mockito" % "mockito-all" % "1.8.4" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.2" % "test"
)
