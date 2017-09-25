package controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents}

class ApplicationController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
}
