import com.sun.tools.classfile.Dependencies

name := """hello-akka"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

libraryDependencies += "org.camunda.bpm.model" % "camunda-bpmn-model" % "7.1.0-alpha4"

libraryDependencies += "org.camunda.bpm.model" % "camunda-xml-model" % "7.3.0"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

scalaVersion := "2.10.2"

resolvers += "spray repo" at "http://nightlies.spray.io"

libraryDependencies ++= Dependencies.demoAkka

fork in run := true