package example

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory

object Main extends App with Api {

  implicit val system = ActorSystem("pbft-distributed-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val config = ConfigFactory.load("application.conf")

  override val nodeActor: ActorRef = system.actorOf(
    Node.props(
      config.getInt("node.number"),
      config.getBoolean("node.isLeader"),
      Seq()
    ),
    "node"
  )
  override val proxyNodeActor: ActorRef = system.actorOf(ProxyNode.props)
  Http().bindAndHandle(
    routes,
    config.getString("http.bindAddress"),
    config.getInt("http.bindPort")
  )
}
