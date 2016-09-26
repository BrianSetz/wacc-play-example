package controllers

import play.api.mvc._

class Application extends Controller {
  /**
    * Simple index controller which returns the index.scala.html page with the specified message
    * @return The result, in this case the HTML page
    */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
}
