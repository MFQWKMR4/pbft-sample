package example

import akka.actor.{ActorRef, Terminated}
import akka.pattern.pipe

object P2P {

  sealed abstract class P2PMsg extends BaseMsg

  case class AddPeer(address: String) extends P2PMsg

  case class ResolvePeer(actorRef: ActorRef) extends P2PMsg

  case class Peers(peers: Seq[String]) extends P2PMsg

  case object GetPeers extends P2PMsg

  case object HandShake extends P2PMsg

}

trait P2P {
  this: BaseActor =>
  import example.P2P._

  var peers: Set[ActorRef] = Set(self)

  def broadcast(msg: BaseMsg): Unit = {
    peers.foreach(_ ! msg)
  }

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
