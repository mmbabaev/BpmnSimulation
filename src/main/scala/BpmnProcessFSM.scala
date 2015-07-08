import java.io.File
import Extensions._
import akka.actor.FSM.Event
import akka.actor._
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.instance._
import org.camunda.bpm.model.xml.instance.ModelElementInstance

/**
 * Created by Mihail on 05.07.15.
 */


// States:

trait ProcessState
case object Idle extends ProcessState
case object Active extends ProcessState
case object End extends ProcessState

// Commands:
case object Commands {
  case object StartFSM
  
  case class StartTask(task: Task)
  case object TaskComplete

  case class WayChosen(condition: String)

  case object End
}

object BpmnProcessFSM {
  def props(start: StartEvent) = Props(new BpmnProcessFSM(start))
}

class BpmnProcessFSM(startNode: StartEvent) extends FSM[ProcessState, FlowNode] {

  startWith(Idle, startNode)

  when(Idle) {
    case Event(Commands.StartFSM, startEvent: StartEvent) => {

      var next = startEvent.nextNode

      next match {
        case task: Task => {
          sender ! Commands.StartTask(task)
        }

        case _ => println("unknown next")
      }

      goto(Active) using startEvent.nextNode
    }
  }

  when(Active) {


    case Event(Commands.End, _) => {
      sender ! "Process was finished"
      println("End event (fsm)")
      stop
    }

    case Event(Commands.TaskComplete, task: Task) => {
      sender ! task.getName + ": Task completed"


      val next = task.nextNode

      next match {
        case task: Task => sender ! Commands.StartTask(task)
        case end: EndEvent =>
          {
            sender ! Commands.End
          }
      }

      stay using next
    }

    case Event(Commands.WayChosen(answer), task: Task) => {
      val gateway = task.nextNode.asInstanceOf[ExclusiveGateway]
      val next = gateway.getNextNode(answer)

      next match {
        case task: Task => {
          sender ! Commands.StartTask(task)
          println("task from gateway")
        }
        case end: EndEvent => {
          sender ! Commands.End
        }
      }
      stay using next
    }

    case _ => stay()
  }

  /*
  def nextElement: ModelElementInstance = {
    stateData match {
      case s: StartEvent => stateData.asInstanceOf[StartEvent].nextElement
      case s: EndEvent   => stateData.asInstanceOf[EndEvent].nextElement
      case s: Task       => stateData.asInstanceOf[Task].nextElement
      // case ExclusiveGatewayState => stateData.asInstanceOf[ExclusiveGateway].nextElement
    }
  }
  */

}

object Main extends App {


  var system = ActorSystem("BpmnSystem")
  var process = system.actorOf(Props[BpmnProcessFSM])

}