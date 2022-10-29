package example

import akka.actor.{ActorRef, Terminated}
import akka.util.Timeout
import akka.pattern.pipe

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

object Pbft {

  sealed abstract class PBFTMsg extends BaseMsg

  case class ToLeader(client: ActorRef, tx: String) extends BaseMsg

  // sent from leader node
  case class Request(tx: String, sig: String) extends PBFTMsg

  // a.k.a トランザクション検証メッセージ
  case class PrePrepare(tx: String) extends PBFTMsg

  // a.k.a クロス検証メッセージ
  case class Prepare(tx: String) extends PBFTMsg

  // a.k.a DB更新命令
  case class Commit(tx: String) extends PBFTMsg

  // a.k.a
  case class Reply(tx: String) extends PBFTMsg

}

//abstract class Pbft(entire: Int, isLeader: Boolean) {
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
    // leaderにクライアントからリクエストが届いたとする
    case ToLeader(client, tx) =>
      dest = Some(client)
      if (isLeader) broadcast(Request(tx, sign(tx)))

    /* トランザクションが正しいこと、内容が改ざんされていないことを検証し、トランザクション内容が検証されたことを他の全てのノードにマルチキャスト */
    case Request(tx, sig) =>
      verify(tx, sig) match {
        case true =>
          state = state.addValidTx(tx)
          broadcast(PrePrepare(tx))
        case false => ()
      }

    /* 他の2f個のノードからトランザクション検証メッセージを受信したノードは、それらのトランザクションの内容が pre-prepare フェーズで自身が検証したものと一致していることを検証して他の全てのノードにマルチキャスト */
    case PrePrepare(tx) =>
      state = state.incrementPrePrepare(tx)
      state.prePrepare.getOrElse(tx, 0) == failLimit * 2 match {
        case true =>
          if (state.validTxs.contains(tx)) broadcast(Prepare(tx))
        case false => ()
      }

    /* 他の2f個のノードからのクロス検証メッセージを受信したノードは、それらのトランザクションの内容が prepare フェーズで自身が検証したものと一致していることを検証して自分が管理するデータベースを更新する命令 */
    case Prepare(tx) =>
      state = state.incrementPrepare(tx)
      state.prepare.getOrElse(tx, 0) == failLimit * 2 match {
        case true =>
          if (state.validTxs.contains(tx)) self ! Commit(tx)
        case false => ()
      }

    /* DB更新 */
    case Commit(tx) =>
      // todo: save tx
      self ! Reply(tx)

    // clientではなく、leaderに返信することになるのでまちがっている(proxyアクターを立てれば解決する問題)
    case Reply(tx) => dest.map((_: ActorRef) ! tx)
  }
}
