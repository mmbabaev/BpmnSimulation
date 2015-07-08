import java.net.InetSocketAddress
import Extensions._
import akka.actor.{Props, ActorRef, Actor}
import akka.io.Tcp

import akka.io.Tcp._
import akka.util.ByteString
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway

/**
 * Created by Mihail on 08.07.15.
 */

object Client {
  def props(remote: InetSocketAddress, listener: ActorRef) =
    Props(classOf[Client], remote, listener)
}

class Client(socket: InetSocketAddress, listener: ActorRef) extends Actor {

  import Tcp._
  import context.system

  var terminal: ActorRef = null // actor of terminal, we can send Write(ByteString) messages there and terminal will print them
  var fsm: ActorRef = null // current process (array of ActorRef in future????

  def writelnInTerminal(s: String) = terminal ! Write(ByteString(s + "\n"))

  var name: String = null

  //IO(Tcp) ! Connect(remote)

  def receive = {

    case Commands.ClientConnected(t) =>
      println("Client connected")
      terminal = t
      name = socket.getPort.toString
      terminal ! Write(ByteString("Your name: " + name + "\n"))

    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context stop self

    case Received(messageWithEnter) => {
      def translateMessage(messageWithEnter: ByteString): String = {
        var bytesArray = messageWithEnter.toArray
        var newBytesArray = new Array[Byte](messageWithEnter.toArray.length - 2)

        bytesArray.copyToArray(newBytesArray, 0, bytesArray.length)
        new String(newBytesArray)
      }

      val message = translateMessage(messageWithEnter)

      message match {
        case "processes" =>
          listener ! Commands.AvailableProcesses

        case command: String if command.containsOneSpace => {
          val command1 = command.split(' ')(0)
          val command2 = command.split(' ')(1)

          command1 match {
            case "start" => listener ! Commands.StartProcess(command2)

            // FSM commands
            case "condition" =>
              fsm ! FsmCommands.WayChosen(command2)

            case "task" =>
              if (command2 == "done") {
                fsm ! FsmCommands.TaskComplete
              }
              else {
                println("not done")
              }
          }

//          val message = "Unknown command!\n"
//          listener ! message
//          sender ! Write(ByteString(message))
        }
      }
    }

    case Commands.AvailableProcesses(processes) => {
      var message = "You can start these processes: \n"

      for (process <- processes) {
        message += process + "\n"
      }

      writelnInTerminal(message)
    }

    case Connected => {
      println("Connected!")
    }
    case Bound => {
      println("Client bound")
      sender ! Write(ByteString("Client Bound!\n"))
    }


    // FSM Commands cases:

    case Commands.Fsm(ref) =>
      println("client get fsm!")
      fsm = ref
      fsm ! FsmCommands.StartFSM

    case FsmCommands.StartTask(task) => {
      writelnInTerminal("You must complete task: " + task.getName)


      task.nextNode match {
        case gateway: ExclusiveGateway => {
          var message = "Type any condition (in format <condition yourChoosedWay>: \n"
          for (condition <- gateway.getConditionStrings) {
            message += "<" + condition + ">\n"
          }

          writelnInTerminal(message)
        }


        case _ => {
          writelnInTerminal("Type <task done>")
        }
      }
    }

    case FsmCommands.End => {
      sender ! FsmCommands.End
      writelnInTerminal("End process (Client)")
    }
  }
}