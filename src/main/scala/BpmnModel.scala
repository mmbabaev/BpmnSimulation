import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Yaroslav on 09.07.15.
 */
class BpmnModel{
  val processes = new ArrayBuffer[BpmnProcess]()
}

class BpmnProcess (id : String) {
  var startEvent: StartEvent = new StartEvent
  var setLane: LaneSet = null
  val userTasks = new ArrayBuffer[UserTask]()
  val serviceTasks = new ArrayBuffer[ServiceTask]()
  val exclusiveGateWays = new ArrayBuffer[ExclusiveGateWay]()
  val sequenceFlows = new ArrayBuffer[SequenceFlow]()
  val endEvents = new ArrayBuffer[EndEvent]()

}

trait FlowNode {
  var id: String = null
  var name: String = null
  def getName() : String
  def getId() : String
  var lane: Lane = null
}



trait OneOutGoing extends FlowNode{
  var nextNode: FlowNode = null
  def getNextNode : FlowNode
}

trait ManyOutGoing extends FlowNode {
  var nextNodes: ArrayBuffer[FlowNode] = new ArrayBuffer[FlowNode]()
  def getNextNode(str: String): FlowNode
}
class ExclusiveGateWay extends  ManyOutGoing {
  val conditions = new ArrayBuffer[String]()
  var hashMap = new HashMap[String, FlowNode]
  def getName(): String ={
    name
  }
  def getId() : String = {
    id
  }
  def getNextNode(str: String): FlowNode = {
    hashMap(str)
  }
}