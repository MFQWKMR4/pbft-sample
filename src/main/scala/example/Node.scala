package example

import akka.actor.Props

object Node {
  def props(entire: Int, isLeader: Boolean, ledger: Seq[String]) = Props(
    new Node(entire, isLeader, ledger)
  )
}

class Node(e: Int, isL: Boolean, var ledger: Seq[String])
    extends BaseActor
    with Pbft
    with P2P {
  override val entire = e
  override val isLeader = isL
}
