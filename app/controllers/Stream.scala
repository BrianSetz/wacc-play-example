package controllers

import akka.stream.scaladsl.Source
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.api.Logger

import scala.concurrent.duration._
import scala.util.Random

class Stream extends Controller {
  def randomIntStream = Action {
    val source = Source.tick(initialDelay = 0 second, interval = 1 second, tick = "tick")

    Logger.info("Created random integer source")

    Ok.chunked(source.map { tick =>
      val value = Random.nextInt()
      Logger.info(s"Generated random integer: $value")
      Json.obj("number" -> value).toString + "\n"
    }.limit(100))
  }
}
