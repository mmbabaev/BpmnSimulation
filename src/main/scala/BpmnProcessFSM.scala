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


object BpmnProcessFSM {
  def props(start: StartEvent) = Props(new BpmnProcessFSM(start))
}

class BpmnProcessFSM(startNode: StartEvent) extends FSM[ProcessState, FlowNode] {

  startWith(Idle, startNode)

  when(Idle) {
    case Event(FsmCommands.StartFSM, startEvent: StartEvent) => {

      var next = startEvent.nextNode

      next match {
        case task: Task => {
          sender ! FsmCommands.StartTask(task)
        }

        case _ => println("unknown next")
      }

      goto(Active) using startEvent.nextNode
    }
  }

  when(Active) {

    case Event(FsmCommands.End, _) => {
      sender ! "Process was finished"
      println("End event (fsm)")
      stop
    }

    case Event(FsmCommands.TaskComplete, task: Task) => {
      sender ! task.getName + ": Task completed"


      val next = task.nextNode

      next match {
        case task: Task => sender ! FsmCommands.StartTask(task)
        case end: EndEvent => {
          sender ! FsmCommands.End
        }
      }

      stay using next
    }

    case Event(FsmCommands.WayChosen(answer), task: Task) => {
      val gateway = task.nextNode.asInstanceOf[ExclusiveGateway]
      val next = gateway.getNextNode(answer)

      next match {
        case task: Task => {
          sender ! FsmCommands.StartTask(task)
          println("task before gateway")
        }
        case end: EndEvent => {
          sender ! FsmCommands.End
        }
      }
      stay using next
    }

    case _ =>
      println("Unknown command!")
      stay()
  }
}