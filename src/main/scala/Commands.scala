import akka.actor.ActorRef

import scala.collection.mutable
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
  case object AvailableMembers
  case class AvailableMembers(members: mutable.HashSet[String])

  case object AvailableProcesses
  case class AvailableProcesses(processes: ArrayBuffer[String])

  //case class SignUp(name: String, password: String)

  case class StartProcess(processName: String)

  case class ClientConnected(terminal: ActorRef)
  case class Fsm(fsm: ActorRef)

  case class ChangeName(oldName: String, name: String)
}

case object Exception {
  case object NotAllClients
}
case class ComplexCommand(command1: String, command2: String)