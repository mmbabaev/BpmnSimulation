import java.io.File
import akka.actor.{Props, ActorSystem}
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, Bpmn}
import org.camunda.bpm.model.bpmn.instance.{SequenceFlow, EndEvent, Task, StartEvent}
import org.camunda.bpm.model.xml.`type`.ModelElementType
import Extensions._
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable
import scala.util.Random

/**
 * Created by Mihail on 06.07.15.
 */
object Client extends App {
  var system = ActorSystem("MySystem")

  var model = Bpmn.readModelFromFile(new File(args(0)))
  Extensions.loadDictionary(model)
  var clientActor = system.actorOf(ClientActor.props(model.getModelElementById("start").asInstanceOf[StartEvent]))
}