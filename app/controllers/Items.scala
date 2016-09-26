package controllers

import models.Item
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import javax.inject.Inject

import play.api.Logger

case class CreateItem(name: String, price: Double, description: Option[String])

trait ItemsJson {
  implicit val writesItem = Json.writes[Item]
  implicit val readsCreateItem = (
      (__ \ "name").read(Reads.minLength[String](1)) and
      (__ \ "price").read(Reads.min[Double](0)) and
      (__ \ "description").readNullable[String]
    )(CreateItem.apply _)

}

class Items @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport with ItemsJson {
  val shop = models.Shop

  val createItemFormModel = Form(mapping(
    "name" -> nonEmptyText(1),
    "price" -> of[Double],
    "description" -> optional(text)
  )(CreateItem.apply)(CreateItem.unapply))

  val updateItemFormModel = Form(mapping(
    "id" -> of[Long],
    "name" -> nonEmptyText(1),
    "price" -> of[Double],
    "description" -> optional(text)
  )(Item.apply)(Item.unapply))

  def list(page: Int) = Action {
    Ok(views.html.list(shop.list))
  }

  def createFromForm = Action(parse.urlFormEncoded) { implicit request =>
    createItemFormModel.bindFromRequest().fold(
      formWithErrors => {
       BadRequest(views.html.createform(formWithErrors))
      },
      createItem => {
        shop.create(createItem.name, createItem.price, createItem.description) match {
          case Some(item) => Ok(views.html.list(shop.list))
          case None => InternalServerError
        }
      }
    )
  }

  def createFromJson = Action(parse.json[CreateItem]) { implicit request =>
    shop.create(request.body.name, request.body.price, request.body.description) match {
      case Some(item) => Ok(Json.toJson(item))
      case None => InternalServerError
    }
  }

  def createForm = Action {
    Ok(views.html.createform(createItemFormModel))
  }

  def details(id: Long) = Action {
    shop.get(id) match {
      case Some(item) =>
        Ok(views.html.details(item, updateItemFormModel.fill(item)))
      case None =>
        NotFound(views.html.notfound("404 - Item not found", s"Item ID: $id"))
    }
  }

  def detailsJson(id: Long) = Action {
    shop.get(id) match {
      case Some(item) =>
        Ok(Json.toJson(item))
      case None =>
        NotFound(views.html.notfound("404 - Item not found", s"Item ID: $id"))
    }
  }

  def updateFromForm = Action(parse.urlFormEncoded) { implicit request =>
    val postAction = request.body.get("action")

    Logger.info(s"[Update Form] Post action: $postAction")

    updateItemFormModel.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.details(Item(-1, "", -1, Some("")), formWithErrors))
      },
      item => {
        postAction.get.head match {
          case "Delete" =>
            shop.delete(item.id) match {
              case true => Ok(views.html.list(shop.list))
              case false => InternalServerError
            }
          case "Update" =>
            shop.update(item.id, item.name, item.price, item.description) match {
              case Some(item) => Ok(views.html.list(shop.list))
              case None => InternalServerError
            }
        }
      }
    )
  }

  def update(id: Long) = Action(parse.json[CreateItem]) { implicit request =>
    shop.update(id, request.body.name, request.body.price, request.body.description) match {
      case Some(item) => Ok(Json.toJson(item))
      case None => InternalServerError
    }
  }

  def delete(id: Long) = Action { implicit request =>
    shop.delete(id) match {
      case true => Ok("deleted")
      case false => InternalServerError
    }
  }
}
