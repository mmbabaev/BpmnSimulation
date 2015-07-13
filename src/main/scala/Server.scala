import java.io.File
import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, HashMap}

/**
 * Created by Mihail on 08.07.15.
 */

object Server {
  def props(socket: InetSocketAddress, processesFileNames: ArrayBuffer[String]) = Props(classOf[Server], socket, processesFileNames)
}
class Server(socket: InetSocketAddress, processesFileNames: ArrayBuffer[String]) extends Actor {
  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, socket)
  val clients = new HashMap[String, ActorRef]()
  val models = new HashMap[String, BpmnModel]

  for (processFileName <- processesFileNames) {
    val model = Parser.getBpmnModel(processFileName)
    models.put(processFileName, model)
  }

  println("Server start working")

  def receive = {
    case Commands.AvailableProcesses =>
      sender ! Commands.AvailableProcesses(processesFileNames)

    case Commands.AvailableMembers =>
      var result = new mutable.HashSet[String]()

      for (model <- models.values) {
        for (laneName <- model.processes(0).laneSet.laneNames) {
          result += laneName
        }
      }

      sender ! Commands.AvailableMembers(result)

    case b @ Bound(localAddress) =>
      println("Server receive Bound(adress)")

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      println("Client connected!")
      sender ! Write(ByteString("Sbd connected!\n"))

      val client = context.actorOf(Client.props(remote, self))

      clients.put(remote.getPort.toString, client)

      sender ! Register(client)
      client ! Commands.ClientConnected(sender)

    case Commands.ChangeName(old, name) => {
      val actor = clients(old)
      clients.remove(old)
      clients.put(name, actor)
      println(old + " become " + name)
    }

    case message: String => {
      println(message)
    }

    case Commands.StartProcess(processFileName) =>
      val model = models(processFileName)

      // TODO: simulate man` processes from one file
      val process = model.processes(0)
      val startEvent = process.startEvent

      val processClients = new mutable.HashMap[String, ActorRef]
      for (laneName <- process.laneSet.laneNames) {
        if (clients.contains(laneName)) {
          processClients.put(laneName, clients(laneName))
        }
      }

      if (processClients.size == process.laneSet.laneNames.length) {
        val fsm = context.actorOf(BpmnProcessFSM.props(startEvent, processClients))

        for (client <- processClients.values) {
          client ! Commands.Fsm(fsm)
        }

        fsm ! FsmCommands.StartFSM
      }
      else {
        sender ! Exception.NotAllClients
      }

  }
}

