package example

import java.util.concurrent.TimeUnit
import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import example.ApiProxy._
import example.P2P.AddPeer

import scala.concurrent.ExecutionContext

trait Api extends Json4sSupport {

  implicit val stringUnmarshallers =
    PredefinedFromEntityUnmarshallers.stringUnmarshaller
  implicit val executionContext: ExecutionContext
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  val nodeActor: ActorRef
  val proxyNodeActor: ActorRef

  val routes =
    get {
      path("result") {
        complete(
          (proxyNodeActor ? GetResult).mapTo[String]
        )
      } ~
        path("info") {
          complete(
            (proxyNodeActor ? GetNodeInfo).mapTo[String]
          )
        }
    } ~
      post {
        path("propose") {
          entity(as[String]) { tx =>
            complete(
              (proxyNodeActor ? Req(tx)).mapTo[String]
            )
          }
        } ~
          path("addPeer") {
            entity(as[String]) { peerAddress =>
              nodeActor ! AddPeer(peerAddress)
              complete(s"Added peer $peerAddress")
            }
          }
      }
}
