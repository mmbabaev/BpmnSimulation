import java.net.InetSocketAddress
import Extensions._
import akka.actor.{Props, ActorRef, Actor}
import akka.io.{IO, Tcp}

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

  var manager: ActorRef = null // actor of terminal (IO manager actor?) , we can send Write(ByteString) messages there and terminal will print them
  var fsm: ActorRef = null // current process (array of ActorRef in future????

  def printInApplication(s: String) = manager ! Write(ByteString(s + "\n"))

  var name: String = null

  //IO(Tcp) ! Connect(socket)

  def receive = {

    case Commands.ClientConnected(t) =>
      println("Client connected")
      manager = t
      name = socket.getPort.toString
      manager ! Write(ByteString("Your name: " + name + "\n"))

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
            case "name" =>
              printInApplication(name + " become " + command2)
              listener ! Commands.ChangeName(name, command2)
              name = command2

            case "start" => listener ! Commands.StartProcess(command2)

            // FSM commands
            case "choose" =>
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

      printInApplication(message)
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

    case FsmCommands.StartTask(task) => {
      printInApplication("You must complete task: " + task.getName)

      task.nextNode match {
        case gateway: ExclusiveGateway => {
          var message = "Choose any condition (in format <choose yourChosenWay>: \n"
          for (condition <- gateway.getConditionStrings) {
            message += "<" + condition + ">\n"
          }

          printInApplication(message)
        }


        case _ => {
          printInApplication("Type <task done>")
        }
      }
    }

    case FsmCommands.End => {
      fsm ! FsmCommands.End
      printInApplication("End process (Client)")
    }

    case s: String => println(s)
  }
}