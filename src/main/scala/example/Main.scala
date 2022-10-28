package example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import example.P2P.AddPeer

object Main extends App {

  implicit val system = ActorSystem("pbft-distributed-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val config = ConfigFactory.load("application.conf")
  val seedHost = config.getString("node.seedHost")
  val nodeActor =
    system.actorOf(Node.props(Seq()), "nodeActor")

  if (!seedHost.isEmpty) {
    nodeActor ! AddPeer(seedHost)
  } else {
    throw new Exception("could not find seed dns host")
  }
}
