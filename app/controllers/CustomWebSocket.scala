package controllers
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}

import scala.util.Random

class CustomWebSocket @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller {
  def echo = WebSocket.accept[String, String] { request =>   // WebSocket.accept[Input, Output]
    ActorFlow.actorRef(out => CustomWebSocketActor.props(out)) // Akka Actor based using CustomWebSocketActor, results in a Flow[Input, Output]
  }

  def randomInteger = WebSocket.accept[String, String] { request =>
    Flow.fromFunction(in => Random.nextInt().toString) // Akka Stream based, create Flow[Input, Output] directly
  }

  def complexJson = WebSocket.accept[JsValue, JsValue] { request => // This WebSocket accepts and returns JsValue (JSON objects)
    Flow.fromFunction { in =>
      Json.obj(
        "original-request" -> in,
        "some-string"  -> Random.nextString(10),
        "some-int"     -> Random.nextInt(),
        "some-boolean" -> Random.nextBoolean(),
        "some-double"  -> Random.nextDouble(),
        "some-object-list" -> Json.arr(
          Json.obj("another-string" -> Random.nextString(5)),
          Json.obj("another-string" -> Random.nextString(5)),
          Json.obj("another-string" -> Random.nextString(5))
        )
      )
    }
  }

  def echoIndex = Action {
    Ok(views.html.websocket("Echo", "ws-echo", "/ws/echo"))
  }

  def randomIntegerIndex() = Action {
    Ok(views.html.websocket("Random Integer", "ws-rand", "/ws/rand"))
  }

  def complexJsonIndex() = Action {
    Ok(views.html.websocket("Complex JSON", "ws-complex", "/ws/complex"))
  }
}
