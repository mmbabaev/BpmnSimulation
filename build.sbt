name := "BpmnProcess"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11"
)

libraryDependencies += "org.camunda.bpm.model" % "camunda-bpmn-model" % "7.1.0-alpha4"

libraryDependencies += "org.camunda.bpm.model" % "camunda-xml-model" % "7.3.0"