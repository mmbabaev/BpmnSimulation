import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

import scala.xml._
/**
 * Created by Yaroslav on 09.07.15.
 */
class Parser (filePath : String) {
  val bpmnModel = new BpmnModel
  loadBpmnModel

  private def loadBpmnModel: Unit = {
    val xml = XML.loadFile(filePath)
    val processes = xml \ "process"
    for (i <- processes) {
      val process = new BpmnProcess((i \ "@id").text)
      loadProcess(process, i)
      bpmnModel.processes.append(process)
    }

  }

  private def loadProcess(process: BpmnProcess, node: NodeSeq): Unit = {
    loadUserTasks(process.userTasks, node)
    loadServiceTasks(process.serviceTasks, node)
    loadExclusiveGateWay(process.exclusiveGateWays, node)
    loadEndEvent(process.endEvents, node)
    loadStartEvent(process.startEvent, node)
    loadSequenceFlows(process, node)
    setNextNodes(process)
    loadSetLane(process, node)
  }

  private def loadUserTasks(tasksArray: ArrayBuffer[UserTask], node: NodeSeq): Unit = {
    val userTasks = node \ "userTask"
    for (i <- userTasks) {
      val userTask = new UserTask
      userTask.id = (i \ "@id").text
      userTask.name = (i \ "@name").text
      tasksArray.append(userTask)
    }
  }

  private def loadServiceTasks(tasksArray: ArrayBuffer[ServiceTask], node: NodeSeq): Unit = {
    val serviceTasks = node \ "serviceTask"
    for (i <- serviceTasks) {
      val serviceTask = new ServiceTask
      serviceTask.id = (i \ "@id").text
      serviceTask.name = (i \ "@name").text

      tasksArray.append(serviceTask)
    }
  }

  private def loadExclusiveGateWay(exclusiveGateWayArray: ArrayBuffer[ExclusiveGateWay], node: NodeSeq): Unit = {
    val exclusiveGateWays = node \ "exclusiveGateway"
    for (i <- exclusiveGateWays) {
      val exclusiveGateway = new ExclusiveGateWay
      exclusiveGateway.id = (i \ "@id").text
      exclusiveGateway.name = (i \ "@name").text
      exclusiveGateWayArray.append(exclusiveGateway)

    }
  }

  private def loadStartEvent(startEvent: StartEvent, node: NodeSeq): Unit = {
    val start = node \ "startEvent"
    for (i <- start) {
      startEvent.id = (i \ "@id").text
      startEvent.name = (i \ "@name").text

    }
  }

  private def loadEndEvent(endEventArray: ArrayBuffer[EndEvent], node: NodeSeq): Unit = {
    val endEvents = node \ "endEvent"

    for (i <- endEvents) {
      val endEvent = new EndEvent
      endEvent.id = (i \ "@id").text
      endEvent.name = (i \ "@name").text
      endEventArray.append(endEvent)
    }
  }

  private def loadSequenceFlows(process: BpmnProcess, node: NodeSeq): Unit = {
    val sequenceFlows = node \ "sequenceFlow"
    for (i <- sequenceFlows) {
      val sequenceFlow = new SequenceFlow
      sequenceFlow.id = (i \ "@id").text
      sequenceFlow.name = (i \ "@name").text
      val condition = i \ "conditionExpression"
      if (condition.length == 1){
        sequenceFlow.condition = condition.text
      }
      val targetRef = (i \ "@targetRef").text
      val sourceRef = (i \ "@sourceRef").text

      //UserTask
      for (j <- process.userTasks) {
        if (sequenceFlow.source == null && j.getId().equals(sourceRef)) {
          sequenceFlow.source = j
        }

        if (sequenceFlow.target == null && j.getId().equals(targetRef)) {
          sequenceFlow.target = j
        }
      }

      //ServiceTask
      if (sequenceFlow.target == null || sequenceFlow.source == null) {
        for (j <- process.serviceTasks) {
          if (sequenceFlow.source == null && j.getId().equals(sourceRef)) {
            sequenceFlow.source = j
          }

          if (sequenceFlow.target == null && j.getId().equals(targetRef)) {
            sequenceFlow.target = j
          }
        }
      }

      //EndEvents
      if (sequenceFlow.target == null) {
        for (j <- process.endEvents) {

          if (sequenceFlow.target == null && j.getId().equals(targetRef)) {
            sequenceFlow.target = j
          }
        }
      }

      //StartEvent
      if (sequenceFlow.source == null && process.startEvent.getId().equals(sourceRef)) {
        sequenceFlow.source = process.startEvent
      }




      //ExclusiveGateWay
      if (sequenceFlow.target == null || sequenceFlow.source == null) {
        for (j <- process.exclusiveGateWays) {
          if (sequenceFlow.source == null && j.getId().equals(sourceRef)) {
            sequenceFlow.source = j
          }

          if (sequenceFlow.target == null && j.getId().equals(targetRef)) {
            sequenceFlow.target = j
          }
        }
      }


      if (sequenceFlow.target == null || sequenceFlow.source == null) {
        println("ERROR LOAD:")
        println("sequenceFlow.id = " + sequenceFlow.getId())

        println("sequenceFlow.name = " + sequenceFlow.getName())
        if (sequenceFlow.source != null) println("sequenceFlow.source " + sequenceFlow.source.getId())
        if (sequenceFlow.target != null) println("sequenceFlow.target " + sequenceFlow.target.getId())
        println("END ERROR \n")
      }
      process.sequenceFlows.append(sequenceFlow)
    }
  }

  private def setNextNodes(process: BpmnProcess): Unit = {
    setNextNodeUserTask(process.sequenceFlows, process.userTasks)
    setNextNodeServiceTask(process.sequenceFlows, process.serviceTasks)
    setNextNodeStartEvent(process.sequenceFlows, process.startEvent)
    setNextNodesExclusiveGateWay(process.sequenceFlows, process.exclusiveGateWays)
  }

  private def setNextNodeUserTask(sequenceFlows: ArrayBuffer[SequenceFlow], userTasks: ArrayBuffer[UserTask]): Unit = {
    for (userTask <- userTasks) {
      breakable {
        for (sequenceFlow <- sequenceFlows) {
          if (sequenceFlow.source.getId().equals(userTask.getId())) {
            userTask.nextNode = sequenceFlow.target
            break()
          }
        }
      }
    }
  }

  private def setNextNodeServiceTask(sequenceFlows: ArrayBuffer[SequenceFlow], serviceTasks: ArrayBuffer[ServiceTask]): Unit = {
    for (serviceTask <- serviceTasks) {
      breakable {
        for (sequenceFlow <- sequenceFlows) {
          if (sequenceFlow.source.getId().equals(serviceTask.getId())) {
            serviceTask.nextNode = sequenceFlow.target
            break()
          }
        }
      }
    }
  }

  private def setNextNodeStartEvent(sequenceFlows: ArrayBuffer[SequenceFlow], startEvent: StartEvent): Unit = {
    breakable {
      for (sequenceFlow <- sequenceFlows) {
        if (sequenceFlow.source.getId().equals(startEvent.getId())) {
          startEvent.nextNode = sequenceFlow.target
          break()
        }
      }
    }
  }

  private def setNextNodesExclusiveGateWay(sequenceFlows: ArrayBuffer[SequenceFlow], exclusiveGateWays: ArrayBuffer[ExclusiveGateWay]): Unit = {
    for (exclusiveGateWay <- exclusiveGateWays) {
      for (sequenceFlow <- sequenceFlows) {
        if (sequenceFlow.source.getId().equals(exclusiveGateWay.getId())) {
          exclusiveGateWay.hashMap += sequenceFlow.condition -> sequenceFlow.target
          exclusiveGateWay.nextNodes.append(sequenceFlow.target)
          exclusiveGateWay.conditions.append(sequenceFlow.condition)
        }
      }
    }
  }

  private def loadSetLane(process: BpmnProcess, node: NodeSeq): Unit = {
    val laneSets = node \ "laneSet"
    for (laneSet <- laneSets) {
      process.laneSet = new LaneSet
      val lanes = laneSets \ "lane"
      for (lane <- lanes) {
        val addLane = new Lane
        addLane.id = (lane \ "@id").text
        addLane.name = (lane \ "@name").text
        process.laneSet.lanes.append(addLane)
        process.laneSet.laneNames.append(addLane.name)
        process.laneSet.laneIds.append(addLane.id)
        val flowNodes = lane \ "flowNodeRef"

        for (flowNode <- flowNodes) {
          var flagAdd = false
          //UserTask
          breakable {
            for (userTask <- process.userTasks) {
              if ((flowNode).text.equals(userTask.getId())) {
                addLane.flowNodes.append(userTask)
                userTask.lane = addLane
                flagAdd = true
                break()
              }
            }
          }

          //serviceTask
          if (flagAdd == false) {
            breakable {
              for (serviceTask <- process.serviceTasks) {
                if ((flowNode).text.equals(serviceTask.getId())) {
                  addLane.flowNodes.append(serviceTask)
                  serviceTask.lane = addLane
                  flagAdd = true
                  break()
                }
              }
            }
          }


          //ExclusiveGateWay
          if (flagAdd == false) {
            breakable {
              for (exclusiveGateWay <- process.exclusiveGateWays) {
                if ((flowNode).text.equals(exclusiveGateWay.getId())) {
                  addLane.flowNodes.append(exclusiveGateWay)
                  exclusiveGateWay.lane = addLane
                  flagAdd = true
                  break()
                }
              }
            }
          }


          //EndEvent
          if (flagAdd == false) {
            breakable {
              for (endEvent <- process.endEvents) {
                if ((flowNode).text.equals(endEvent.getId())) {
                  addLane.flowNodes.append(endEvent)
                  endEvent.lane = addLane
                  flagAdd = true
                  break()
                }
              }
            }
          }


          //StartEvent
          if (flagAdd == false) {
            if ((flowNode).text.equals(process.startEvent.getId())) {
              addLane.flowNodes.append(process.startEvent)
              process.startEvent.lane = addLane
              flagAdd = true
            }


            if(flagAdd == false) {
              println("ERROR LANE")
            }
          }
        }
      }
    }


  }
}

object Parser{
  def getBpmnModel(filePath: String): BpmnModel ={
    val parser = new Parser(filePath)
    parser.bpmnModel
  }
}


object StartParser extends App{
  val bpmnModel = Parser.getBpmnModel("my2.bpmn")
  for (process <- bpmnModel.processes){
    println("New Process start")

    println("User Task")
    for(userTask <-process.userTasks) {
      println("Id = " + userTask.getId())
      println("Name = " + userTask.getName())
      println("Lane = " + userTask.lane.id)

      println("NextNode Id = " + userTask.getNextNode().getId())

      println()
    }
    println("End UserTask\n")


    println("Service Task")
    for(serviceTask <-process.serviceTasks) {
      println("Id = " + serviceTask.getId())
      println("Name = " + serviceTask.getName())
      println("Lane = " + serviceTask.lane.id)

      println("NextNode Id = " + serviceTask.getNextNode().getId())

      println()
    }
    println("End ServiceTask\n")

    println("EndEvents Task")
    for(endEvent <-process.endEvents) {
      println("Id = " + endEvent.getId())
      println("Name = " + endEvent.getName())
      println("Lane = " + endEvent.lane.id)
      if (endEvent.getNextNode() == null){
        println("nextNode = null OK")
      }
      else {
        println("ERROR!!!!!! NextNode Id = " + endEvent.getNextNode().getId())
      }
      println()
    }
    println("End endEvents\n")

    println("Start ExclusiveGateWay")
    for(exclusiveGateWay <-process.exclusiveGateWays) {
      println("Id = " + exclusiveGateWay.getId())
      println("Name = " + exclusiveGateWay.getName())
      println("Lane = " + exclusiveGateWay.lane.id)
      for(i <- exclusiveGateWay.conditions){
        println("Condition = " + i)
        println("NextNode  =  " + exclusiveGateWay.getNextNode(i))
      }
      for (nextNode <- exclusiveGateWay.nextNodes){
        println("NextNode Id = " + nextNode.getId())
      }

      println()
    }
    println("End exclusiveGateWay\n")

    println("StartEvent")
    println("Id = " + process.startEvent.getId())
    println("Name = " + process.startEvent.getName())
    println("Lane = " + process.startEvent.lane.id)
    println("NextNode Id = " + process.startEvent.getId())
    println("Process end\n")
  }
}







