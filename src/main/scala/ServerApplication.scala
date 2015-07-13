import java.net.InetSocketAddress

import akka.actor.ActorSystem

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Mihail on 08.07.15.
 */
object ServerApplication extends App {
  val system = ActorSystem("ProcessesSystem")

  val socket = new InetSocketAddress("localhost", 2222)

  val processes = new ArrayBuffer[String]()

  for (arg <- args) {
    processes.append(arg)
  }

  val serverActor = system.actorOf(Server.props(socket, processes))


  readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  system.shutdown()
}
