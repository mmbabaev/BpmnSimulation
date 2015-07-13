import java.io.File

import akka.actor.{Props, FSM, Actor}
import org.camunda.bpm.model.bpmn.Bpmn._
import org.camunda.bpm.model.bpmn.instance.{Task, ExclusiveGateway, StartEvent, FlowNode}
//import Extensions._
/**
 * Created by Mihail on 05.07.15.
 */

object ClientActor {
  def props(startEvent: StartEvent) = Props(new ClientActor(startEvent))
}
//
class ClientActor(startEvent: StartEvent) extends Actor {

  var fsm = context.actorOf(Props(new BpmnProcessFSM(startEvent)))
  fsm ! FsmCommands.StartFSM
  println("Client actor loaded!")

  def receive = {

    case FsmCommands.StartTask(task) => {
      println("You must complete task: " + task.getName)


      task.nextNode match {
        case gateway: ExclusiveGateway => {
          println("Type any condition: ")
          for (condition <- gateway.getConditionStrings) {
            println("<" + condition + ">")
          }

          var command = Console.readLine
          fsm ! FsmCommands.WayChosen(command)
        }

        case _ => {
          println("Type done")
          var command = Console.readLine
          if (command == "done") {
            fsm ! FsmCommands.TaskComplete
          }
          else {
            println("Incorrect command!")
            self ! FsmCommands.StartTask(task)
          }
        }
      }
    }

    case FsmCommands.End => {
      sender ! FsmCommands.End
      println("End process (Client)")
    }
    case message: String => println(message)
  }
}
