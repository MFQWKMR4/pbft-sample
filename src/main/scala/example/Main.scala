package example

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger

object Main extends App with Api {
  val logger = Logger("Main")
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
  logger.debug(
    s"node actor => ${nodeActor.path.toSerializationFormat}"
  )
  override val proxyNodeActor: ActorRef = system.actorOf(ProxyNode.props)
  logger.debug(
    s"proxy node actor => ${proxyNodeActor.path.toSerializationFormat}"
  )
  Http().bindAndHandle(
    routes,
    config.getString("http.bindAddress"),
    config.getInt("http.bindPort")
  )
  logger.debug(
    s"http server listening on ${config.getString("http.bindAddress")}:${config.getInt("http.bindPort")}"
  )
}
