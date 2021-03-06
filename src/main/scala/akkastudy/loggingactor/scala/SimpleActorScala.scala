package akkastudy.loggingactor.scala

import akka.actor.Actor
import akka.event.Logging

class SimpleActorScala extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case "test" ⇒ log.info("received message test in Simple Actor Scala")
    case _ ⇒ log.info("received unknown message test in Simple Actor Scala")
  }
}