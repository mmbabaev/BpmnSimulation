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

  var model = Bpmn.readModelFromFile(new File("test.bpmn"))
  Extensions.loadDictionary(model)
  var clientActor = system.actorOf(ClientActor.props(model.getModelElementById("start").asInstanceOf[StartEvent]))

  def hashTest(): Unit = {
    var dict = mutable.ParHashMap("_id0" -> 0)
    for (i <- 1 to 999999) {
      dict.put("_id" + i, i)
    }

    val gen = new Random()
    val i = dict("_id" + gen.nextInt(999999))
    println(i)
    println("OK")

    val hashCodes = mutable.ParHashMap(0 -> 0)
    for (key <- dict.keys.toArray) {
      try {
        hashCodes(key.hashCode) += 1
      }
      catch {
        case e => hashCodes.put(key.hashCode, 1)
      }
    }
    for (value <- hashCodes.values.toArray) {
      println(value)
    }
    println("Max: " + hashCodes.values.max)
  }
//  var startEvent = model.getModelElementById("start").asInstanceOf[StartEvent]
//  println(startEvent.getId)
//  println(startEvent.getNextNode.asInstanceOf[Task].getName)
//
//  var clientActor = system.actorOf(Props(new ClientActor(startEvent)))
//

}
//
//class BpmnModel(model: BpmnModelInstance) {
//  val pools = new ArrayBuffer[Pool]()
//  pools.append(new Pool(model))
//}
//
//class Pool (model: BpmnModelInstance) {
//
//  val tasks =  new ArrayBuffer[Task]()
//  val startEventLoader: StartEvent = model.getModelElementById("start").asInstanceOf[StartEvent]
//  val startEvent = new StartEvent(startEventLoader)
//  startEvent.setNext(model)
//
//  val endEvents = new ArrayBuffer[EndEvent]()
//
//
//  val seqFlowType = model.getModel().getType(classOf[SequenceFlow])
//  val collectionSequenceFlows = model.getModelElementsByType(seqFlowType).toArray()
//  val sequenceFlows = new ArrayBuffer[SequenceFlow]()
//
//  for (flowAny <- collectionSequenceFlows){
//    flowAny match {
//      case flow: SequenceFlow => {
//        sequenceFlows.append(flow)
//      }
//      case _ => println("unknown!")
//    }
//  }
//
//  loadTasks
//  loadEndEvent
//
//
//
//  def loadTasks = {
//    val taskType = model.getModel.getType(classOf[Task])
//    val taskCollection = model.getModelElementsByType(taskType).toArray
//
//    for (taskAny <- taskCollection) {
//      val taskLoader = taskAny.asInstanceOf[Task]
//      val task = new Task(taskLoader)
//      task.setNext(model)
//      tasks.append(task)
//    }
//  }
//
//  def loadEndEvent: Unit ={
//    val endEventType = model.getModel.getType(classOf[EndEvent])
//    val endEventCollection = model.getModelElementsByType(endEventType).toArray
//    for (endEv <- endEventCollection){
//      val end = endEv.asInstanceOf[EndEvent]
//         endEvents.append(new EndEvent(end))
//    }
//  }
// }