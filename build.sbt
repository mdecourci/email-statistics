import sbt._
import Keys._

name := "email-statistics"


//mainClass := Some("com.service.email.Emailer")

mainClass in(Compile, packageBin) := Some("com.service.email.Emailer")

version := "1.0"

scalaVersion := "2.11.7"
val sparkVersion = "2.2.1"

version := "0.1"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
    "org.scala-lang.modules" %% "scala-xml" % "1.1.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
)
