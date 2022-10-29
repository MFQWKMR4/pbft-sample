package example

import akka.actor.ActorRef

object Pbft {

  sealed abstract class PBFTMsg extends BaseMsg

  case class ToLeader(client: ActorRef, tx: String) extends BaseMsg

  case class Request(tx: String, sig: String) extends PBFTMsg

  case class PrePrepare(tx: String) extends PBFTMsg

  case class Prepare(tx: String) extends PBFTMsg

  case class Commit(tx: String) extends PBFTMsg

  case class Reply(tx: String) extends PBFTMsg

}

trait Pbft {
  this: P2P with BaseActor =>

  import example.Pbft._

  val isLeader: Boolean
  val entire: Int
  val failLimit: Int = (entire - 1) / 3
  def verify(tx: String, sig: String) = true
  def sign(tx: String) = tx + "_sig"

  case class LocalState(
      validTxs: Seq[String],
      prePrepare: Map[String, Int],
      prepare: Map[String, Int]
  ) {
    def addValidTx(tx: String) = copy(validTxs = validTxs :+ tx)
    def incrementPrePrepare(tx: String) =
      copy(prePrepare = prePrepare.updated(tx, prePrepare.getOrElse(tx, 0) + 1))
    def incrementPrepare(tx: String) =
      copy(prepare = prepare.updated(tx, prepare.getOrElse(tx, 0) + 1))
  }

  var state = LocalState(Seq(), Map.empty, Map.empty)
  var dest: Option[ActorRef] = None

  receiver {
    /* sent from proxy */
    case ToLeader(client, tx) =>
      dest = Some(client)
      if (isLeader) broadcast(Request(tx, sign(tx)))

    /* PrePrepare */
    case Request(tx, sig) =>
      verify(tx, sig) match {
        case true =>
          state = state.addValidTx(tx)
          broadcast(PrePrepare(tx))
        case false => ()
      }

    /* Prepare */
    case PrePrepare(tx) =>
      state = state.incrementPrePrepare(tx)
      state.prePrepare.getOrElse(tx, 0) == failLimit * 2 match {
        case true =>
          if (state.validTxs.contains(tx)) broadcast(Prepare(tx))
        case false => ()
      }

    /* will Commit */
    case Prepare(tx) =>
      state = state.incrementPrepare(tx)
      state.prepare.getOrElse(tx, 0) == failLimit * 2 match {
        case true =>
          if (state.validTxs.contains(tx)) self ! Commit(tx)
        case false => ()
      }

    case Commit(tx) =>
      // todo: save tx
      self ! Reply(tx)

    /* reply to proxy */
    case Reply(tx) => dest.map((_: ActorRef) ! tx)
  }
}
