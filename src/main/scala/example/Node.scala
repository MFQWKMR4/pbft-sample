package example

import akka.actor.Props

object Node {
  def props(ledger: Seq[String]) = Props(new Node(ledger))
}

class Node(var ledger: Seq[String]) extends BaseActor with P2P with Pbft {}
