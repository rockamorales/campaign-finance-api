ThisBuild / scalaVersion := "2.13.11"

lazy val tapirVersion = "1.5.4"
lazy val AkkaManagementVersion = "1.4.1"
lazy val akkaVersion = "2.8.2"
lazy val akkaHttpVersion = "10.5.2"
lazy val scalaLogging = "3.9.5"
lazy val tapirOpenAPICirceYaml = "1.0.0-M9"
lazy val tapirAkkaHTTPServer = "1.5.4"
lazy val logbackClassic = "1.4.7"
lazy val macWire = "2.5.8"
val circe        = "0.14.3"
val kittens      = "2.0.0"

lazy val rootProject = (project in file("."))
  .settings(
    name := "campaign-finance-api",
    version := "0.1.0-SNAPSHOT",
    organization := "com.smartsoft",
    scalaVersion := "2.13.11",
    libraryDependencies ++= Seq(

      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
      "com.typesafe.akka" %% "akka-coordination" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "com.typesafe.akka" %% "akka-pki" % akkaVersion,

      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLogging,

      "com.softwaremill.macwire" %% "macros" % macWire % "provided",
      "com.softwaremill.macwire" %% "macrosakka" % macWire % "provided",

      "io.jsonwebtoken" % "jjwt-api" % "0.11.5",
      "io.jsonwebtoken" % "jjwt-impl" % "0.11.5",
      "io.jsonwebtoken" % "jjwt-gson" % "0.11.5",

      "com.lightbend.akka.management" %% "akka-management" % AkkaManagementVersion,

      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirOpenAPICirceYaml,
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirAkkaHTTPServer exclude("com.typesafe.akka", "akka-stream_2.12"),

      "io.circe" %% "circe-core" % circe,
      "io.circe" %% "circe-generic" % circe,
      "io.circe" %% "circe-refined" % circe,
      "io.circe" %% "circe-parser" % circe,

      "org.mindrot" % "jbcrypt" % "0.4",

      "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.1.1",
      "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % "1.1.1" % Test,
      "com.esri.geometry" % "esri-geometry-api" % "2.2.4",

      "ch.qos.logback" % "logback-classic" % logbackClassic,

      "org.scalatest" %% "scalatest" % "3.2.16" % Test
    )
  )
