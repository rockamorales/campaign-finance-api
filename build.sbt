ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val tapirVersion = "1.5.4"
lazy val AkkaManagementVersion = "1.4.1"
lazy val akkaVersion = "2.8.2"
lazy val akkaHttpVersion = "10.5.2"



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

      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

      "com.lightbend.akka.management" %% "akka-management" % AkkaManagementVersion,

      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % "1.0.0-M9",
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % "1.5.4" exclude("com.typesafe.akka", "akka-stream_2.12"),
      "ch.qos.logback" % "logback-classic" % "1.4.7",

      "org.scalatest" %% "scalatest" % "3.2.16" % Test
    )
  )
