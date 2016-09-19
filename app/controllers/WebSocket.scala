package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}

class WebSocket @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {
  def echo = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => WebSocketActor.props(out))
  }

  // TODO: Random Integers
  // TODO: JSON objects

  def index = Action {
    Ok(views.html.websocket())
  }
}
