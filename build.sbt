name := "dist-evolve"

version := "0.1"

scalaVersion := "2.13.5"

idePackagePrefix := Some("org.margorczynski.distevolve")

val AkkaVersion = "2.5.31"
val KafkaVersion = "2.4.1"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.7",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.2.5" % "test"
)