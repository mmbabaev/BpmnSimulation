import org.camunda.bpm.model.bpmn.instance.Task

trait ProcessState
case object Idle extends ProcessState
case object Active extends ProcessState
case object End extends ProcessState

// Commands:
case object FsmCommands {
  case object StartFSM

  case class StartTask(task: Task)
  case object TaskComplete

  case class WayChosen(condition: String)

  case object End
}
