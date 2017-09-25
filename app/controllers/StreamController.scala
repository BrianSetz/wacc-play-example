package controllers

import javax.inject.Inject

import akka.stream.scaladsl.Source
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.Logger

import scala.concurrent.duration._
import scala.util.Random

class StreamController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {
  def randomIntStream = Action {
    // Tick source generates a "tick" every x seconds
    val source = Source.tick(initialDelay = 0 second, interval = 1 second, tick = "tick")

    Logger.info("Created random integer source")

    // Ok.chunked(...) accepts Akka Source
    Ok.chunked(source.map { tick => // For every tick....
      val value = Random.nextInt() // Generate random integer
      Logger.info(s"Generated random integer: $value")

      // Construct JSON object
      Json.obj("number" -> value).toString + "\n"
    }.limit(100)) // Stop after 100 values
  }
}