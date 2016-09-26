package controllers

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger

object CustomWebSocketActor {
  def props(out: ActorRef) = Props(new CustomWebSocketActor(out))
}

class CustomWebSocketActor(out: ActorRef) extends Actor {
  Logger.info("Created WebSocket Actor")

  def receive = {
    case msg: String =>
      Logger.info(s"Received $msg, echoing")
      out ! msg
    case x => Logger.warn(s"Unknown message $x")
  }

  override def postStop() = {
    Logger.info("WebSocket has closed")
  }
}
