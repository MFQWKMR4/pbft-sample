package example

import akka.actor.Props

object ApiProxy {

  sealed abstract class ApiMsg extends BaseMsg

  case class Req(tx: String) extends ApiMsg

  case class Res(tx: String) extends ApiMsg

  case object GetResult extends ApiMsg

}

trait ApiProxy {
  this: P2P with BaseActor =>

  import example.ApiProxy._
  import example.Pbft._

  var result = "not yet"

  receiver {
    // leaderにおくる
    case Req(tx) =>
      broadcast(ToLeader(self, tx))
      sender() ! "OK"

    // PBFTからResponseがとどく
    case Res(tx) => result = tx

    case GetResult => sender() ! result
  }
}

object ProxyNode {
  def props = Props(new ProxyNode)
}

class ProxyNode extends BaseActor with ApiProxy with P2P
