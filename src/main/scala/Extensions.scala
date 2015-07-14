<<<<<<< HEAD
import java.util
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Mihail on 05.07.15.
 */



object Extensions {
  implicit class StringExtension(s: String) {
    def containsOneSpace(): Boolean = {
      s.contains(' ') && (s.lastIndexOf(' ') == s.indexOf(' '))
    }

  }
}


=======
//import java.util
//
//import org.camunda.bpm.model.bpmn.BpmnModelInstance
//import org.camunda.bpm.model.bpmn.instance._
//import org.camunda.bpm.model.xml.instance.ModelElementInstance
//
//import scala.collection.mutable.ArrayBuffer
//
///**
// * Created by Mihail on 05.07.15.
// */
//
//
//
//object Extensions {
//  def loadDictionary(model: BpmnModelInstance) = {
//    globalModel = model
//    val flowType = model.getModel.getType(classOf[SequenceFlow])
//    val flows = model.getModelElementsByType(flowType).toArray
//    for (flowAny <- flows) {
//      val flow = flowAny.asInstanceOf[SequenceFlow]
//      allFlows.append(flow)
//      dictionary.put(flow.getSource.getId, flow.getTarget)
//    }
//    println("cycle")
//  }
//
//  var allFlows = ArrayBuffer[SequenceFlow]()
//  var globalModel: BpmnModelInstance = null
//  var dictionary = new util.HashMap[String, FlowNode]()
//
//  implicit class StartEventExtension(node: StartEvent) {
//    var nextNode: FlowNode = dictionary.get(node.getId)
//  }
//
//  implicit class EndEventExtension(node: EndEvent) {
//    var nextNode: FlowNode = dictionary.get(node.getId)
//  }
//
//  implicit class TaskExtension(node: Task) {
//    var nextNode: FlowNode = dictionary.get(node.getId)
//  }
//
//  implicit class ExclusiveGatewayExtension(gateway: ExclusiveGateway) {
//    def getConditionStrings: ArrayBuffer[String] = {
//      val result = ArrayBuffer[String]()
//      for (flow <- allFlows) {
//        if (flow.getSource.getId.equals(gateway.getId)) {
//          if (flow.getConditionExpression != null) {
//            result.append(flow.getConditionExpression.getTextContent)
//          }
//        }
//      }
//
//      return result
//    }
//    def getNextNode(condition: String): FlowNode = {
//      val flowType = globalModel.getModel.getType(classOf[SequenceFlow])
//
//      for (flow <- globalModel.getModelElementsByType(flowType).toArray) {
//        val flowCondition = flow.asInstanceOf[SequenceFlow].getConditionExpression
//        if (flowCondition != null) {
//          val flowConditionText = flowCondition.getTextContent
//
//          if (condition.equals(flowConditionText)) {
//
//            return flow.asInstanceOf[SequenceFlow].getTarget
//          }
//        }
//      }
//
//      return null
//    }
//  }
//}
//
>>>>>>> 90170174f13f4a37be12e7fc38dcac2f5c7dffee
