package example

import akka.actor.Props

object ApiProxy {

  sealed abstract class ApiMsg extends BaseMsg

  case class Req(tx: String) extends ApiMsg

  case class Res(tx: String) extends ApiMsg

  case object GetResult extends ApiMsg

  case object GetNodeInfo extends ApiMsg

}

trait ApiProxy {
  this: P2P with BaseActor =>

  import example.ApiProxy._
  import example.Pbft._

  var result = "not yet"

  receiver {
    // to PBFT algorithm
    case Req(tx) =>
      broadcast(ToLeader(self, tx))
      sender() ! "proposed"

    // from PBFT algorithm
    case Res(tx) => result = tx

    case GetResult => sender() ! result

    case GetNodeInfo => sender() ! self.toString()
  }
}

object ProxyNode {
  def props = Props(new ProxyNode)
}

class ProxyNode extends BaseActor with ApiProxy with P2P
