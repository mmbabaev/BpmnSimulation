import java.io.File
import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props, Actor}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.instance.StartEvent

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable

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
  println("Server start working")

  def receive = {
    case Commands.AvailableProcesses =>
      println("receive Available processes")
      sender ! Commands.AvailableProcesses(processesFileNames)

    case b @ Bound(localAddress) =>
      println("Server was bounded")

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      println("Client connected!")
      sender ! Write(ByteString("Sbd connected!\n"))

      val client = context.actorOf(Client.props(remote, self))

      sender ! Register(client)
      client ! Commands.ClientConnected(sender)

    case message: String => {
      println(message)
    }

    case Commands.StartProcess(processFileName) =>
      var system = ActorSystem("MySystem")

      var model = Bpmn.readModelFromFile(new File(processFileName))
      Extensions.loadDictionary(model)

      val startEvent = model.getModelElementById("start").asInstanceOf[StartEvent]
      val fsm = context.actorOf(Props(new BpmnProcessFSM(startEvent)))
      println("server almost end startprocess ")
      sender ! Commands.Fsm(fsm)
  }
}
