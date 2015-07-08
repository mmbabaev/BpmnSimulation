//import org.camunda.bpm.model.bpmn.BpmnModelInstance
//import org.camunda.bpm.model.bpmn.instance._
//
///**
// * Created by Mihail on 06.07.15.
// */
//trait MyFlowNode{
//
//}
//
//case class MyFlowNodeClass (node: FlowNode) {
//  val getId = node.getId
//  val getName = node.getName
//}
//
//
//class FlowNodeOneOut(node1: FlowNode) extends FlowNode (node1){
//  var nextNode: FlowNode = null
//
//  def setNext(model: BpmnModelInstance): Unit = {
//    val flowType = model.getModel.getType(classOf[SequenceFlow])
//    val flows = model.getModelElementsByType(flowType).toArray
//
//    for (flowAny <- flows) {
//      val flow = flowAny.asInstanceOf[SequenceFlow]
//
//      if (flow.getSource.getId.equals(node1.getId)) {
//        nextNode = new FlowNode(flow.getTarget)
//        return
//      }
//    }
//  }
//}
//
//class TaskWN (task: Task) extends FlowNodeOneOut(task) with MyFlowNode {
//
//}
//
//class StartEventWN (startEvent: StartEvent) extends FlowNodeOneOut(startEvent) with MyFlowNode {
//
//}
//
//class EndEventWN (endEvent: EndEvent) extends FlowNode(endEvent) with MyFlowNode {
//
//}
