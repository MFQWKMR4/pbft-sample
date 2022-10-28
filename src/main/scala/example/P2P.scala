package example

import akka.actor.{ActorRef, Terminated}
import akka.util.Timeout
import akka.pattern.pipe
import example.P2P.Msg

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

object P2P {

  sealed abstract class Msg

  case class AddPeer(address: String) extends Msg

  case class ResolvePeer(actorRef: ActorRef) extends Msg

  case class Peers(peers: Seq[String]) extends Msg

  case object GetPeers extends Msg

  case object HandShake extends Msg

}

trait P2P {
  this: BaseActor =>

  var peers: Set[ActorRef] = Set.empty

  def broadcast(msg: Msg): Unit = {
    peers.foreach(_ ! msg)
  }

  import example.P2P._
//  implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))
//  implicit val executionContext = context.system.dispatcher

  receiver {
    case AddPeer(peerAddress) =>
      context
        .actorSelection(peerAddress)
        .resolveOne()
        .map(ResolvePeer)
        .pipeTo(self)

    case ResolvePeer(newPeerRef: ActorRef) =>
      if (!peers.contains(newPeerRef)) {
        context.watch(newPeerRef)
        newPeerRef ! HandShake
        newPeerRef ! GetPeers
        broadcast(AddPeer(newPeerRef.path.toSerializationFormat))
        peers += newPeerRef
      } else ()

    case Peers(peers) => peers.foreach(self ! AddPeer(_: String))

    case HandShake =>
      peers += sender()

    case GetPeers =>
      sender() ! Peers(peers.toSeq.map(_.path.toSerializationFormat))

    case Terminated(actorRef) =>
      peers -= actorRef
  }
}
