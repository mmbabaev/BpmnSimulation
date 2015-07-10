name := "BpmnProcess"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11"
)

libraryDependencies += "org.camunda.bpm.model" % "camunda-bpmn-model" % "7.1.0-alpha4"

libraryDependencies += "org.camunda.bpm.model" % "camunda-xml-model" % "7.3.0"