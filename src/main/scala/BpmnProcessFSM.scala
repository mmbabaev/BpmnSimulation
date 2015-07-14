
import java.io.File
import akka.actor.FSM.Event
import akka.actor._

import scala.collection.mutable

/**
 * Created by Mihail on 05.07.15.
 */

object BpmnProcessFSM {
  def props(start: StartEvent, clients: mutable.HashMap[String, ActorRef]) = Props(new BpmnProcessFSM(start, clients))
}

class BpmnProcessFSM(startNode: StartEvent, clients: mutable.HashMap[String, ActorRef]) extends FSM[ProcessState, FlowNode] {

  startWith(Idle, startNode)

  when(Idle) {
    case Event(FsmCommands.StartFSM, startEvent: StartEvent) => {

      var next = startEvent.nextNode

      next match {
        case task: Task => {
          clients(task.getLaneName) ! FsmCommands.StartTask(task)
        }

        case _ => println("unknown next")
      }

      goto(Active) using startEvent.nextNode
    }
  }

  when(Active) {

    case Event(FsmCommands.End, _) => {
      for(client <- clients.values) {
        client ! "Process was finished"
      }

      println("End event (fsm)")
      stop
    }

    case Event(FsmCommands.TaskComplete, task: Task) => {
      clients(task.getLaneName) ! task.getName + ": Task completed"


      val next = task.nextNode

      next match {
        case task: Task => clients(task.getLaneName) ! FsmCommands.StartTask(task)
        case end: EndEvent => {
          clients(end.getLaneName) ! FsmCommands.End
        }
      }

      stay using next
    }

    case Event(FsmCommands.WayChosen(answer), task: Task) => {
      val gateway = task.nextNode.asInstanceOf[ExclusiveGateWay]
      val next = gateway.getNextNode(answer)

      next match {
        case t: Task => {
          println("case task")
          clients(t.getLaneName) ! FsmCommands.StartTask(t)
        }

        case end: EndEvent => {
          println("case end")
          clients(end.getLaneName) ! FsmCommands.End
        }
      }
      stay using next
    }

    case Event(s: String, _) =>

      sender ! "Who are you"
      println("Unknown command!" + s)
      stay()
  }
}
