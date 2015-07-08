import akka.actor.ActorRef
import org.camunda.bpm.model.bpmn.instance.Task

import scala.collection.mutable.ArrayBuffer

trait ProcessState
case object Idle extends ProcessState
case object Active extends ProcessState
case object End extends ProcessState

// FsmCommands:
case object FsmCommands {
  case object StartFSM

  case class StartTask(task: Task)
  case object TaskComplete

  case class WayChosen(condition: String)

  case object End
}

// Commands: (for server-client)

case object Commands {
  case object AvailableProcesses
  case class AvailableProcesses(processes: ArrayBuffer[String])

  //case class SignUp(name: String, password: String)

  case class StartProcess(processName: String)

  case class ClientConnected(terminal: ActorRef)
  case class Fsm(fsm: ActorRef)
}

case class ComplexCommand(command1: String, command2: String)