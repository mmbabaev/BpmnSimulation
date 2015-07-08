import java.net.InetSocketAddress

import akka.actor._
import akka.io.{IO, Tcp}
import akka.util.ByteString

object EchoServiceApp extends App {

  val system = ActorSystem("echo-service-system")
  val serverActor = system.actorOf(Props[Server])


  readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  system.shutdown()
}

object ClientTcp {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[ClientTcp], remote, replies)
}

class ClientTcp(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context stop self

    case c @ Connected(remote, local) =>
      listener ! c
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: ByteString =>
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          // O/S buffer was full
          listener ! "write failed"
        case Received(data) =>
          listener ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          listener ! "connection closed"
          context stop self
      }
  }
}

class Server extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 11111))
  println("Server start working")

  def receive = {
    case b @ Bound(localAddress) =>
      println("Server was bounded")

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      println("connected!")
      val handler = context.actorOf(Props[SimplisticHandler])
      val connection = sender()
      connection ! Register(handler)
  }
}

class SimplisticHandler extends Actor {
  import Tcp._
  def receive = {
    case Received(data) => sender() ! Write(data)
    case PeerClosed     => context stop self
  }
}