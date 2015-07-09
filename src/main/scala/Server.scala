import java.io.File
import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props, Actor}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.instance.StartEvent

import scala.collection
import scala.collection.mutable.ArrayBuffer
import collection.mutable.HashMap

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

  println("Server start working")

  def receive = {
    case Commands.AvailableProcesses =>
      sender ! Commands.AvailableProcesses(processesFileNames)

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
      val system = ActorSystem("MySystem")

      val model = Bpmn.readModelFromFile(new File(processFileName))
      Extensions.loadDictionary(model)

      val startEvent = model.getModelElementById("start").asInstanceOf[StartEvent]
      val fsm = context.actorOf(BpmnProcessFSM.props(startEvent, clients))
      for (client <- clients.values) {
        client ! Commands.Fsm(fsm)
      }
      fsm ! FsmCommands.StartFSM
  }
}
