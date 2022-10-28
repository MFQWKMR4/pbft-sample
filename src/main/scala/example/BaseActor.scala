package example

import akka.actor.AbstractActor.emptyBehavior
import akka.actor.Actor
import akka.util.Timeout

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

class BaseActor extends Actor {

  implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))
  implicit val executionContext = context.system.dispatcher

  var receivers: Receive = emptyBehavior

  def receiver(next: Receive) { receivers = receivers orElse next }

  final def receive = receivers
}
