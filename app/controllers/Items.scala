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

/**
  * Case class used when creating new items
  * @param name Name of the new item
  * @param price Price of the new item
  * @param description Optional description of the new item
  */
case class CreateItem(name: String, price: Double, description: Option[String])

/**
  * A trait defining how the Item case class is serialized to and from JSON, using the Play JSON API
  */
trait ItemsJson {
  implicit val writesItem = Json.writes[Item]
  implicit val readsCreateItem = (
      (__ \ "name").read(Reads.minLength[String](1)) and
      (__ \ "price").read(Reads.min[Double](0)) and
      (__ \ "description").readNullable[String]
    )(CreateItem.apply _)

}

/**
  * The controller which handles operations for the CRUD demo
  *
  * @param messagesApi MessagesApi is automatically injected by Play
  */
class Items @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport with ItemsJson {
  val shop = models.Shop // The model representing the shop

  // Defines the form used for creating new items
  val createItemFormModel = Form(mapping(
    "name" -> nonEmptyText(1),
    "price" -> of[Double],
    "description" -> optional(text)
  )(CreateItem.apply)(CreateItem.unapply))

  // Defines the form for updating existing items
  val updateItemFormModel = Form(mapping(
    "id" -> of[Long], // Existing items have ID's
    "name" -> nonEmptyText(1),
    "price" -> of[Double],
    "description" -> optional(text)
  )(Item.apply)(Item.unapply))

  /**
    * List all items in the shop
    *
    * @param page Pagination, is not used in this demo
    * @return The resulting HTML page
    */
  def list(page: Int) = Action {
    Ok(views.html.list(shop.list))
  }

  /**
    * Create new items from the submission of the createItemFormModel
    *
    * @return The result of item creation, Ok(...) upon success, BadRequest/InternalServerError when not successful
    */
  def createFromForm = Action(parse.urlFormEncoded) { implicit request =>
    createItemFormModel.bindFromRequest().fold(
      formWithErrors => { // If there are form errors
       BadRequest(views.html.createform(formWithErrors)) // Show errors
      },
      createItem => { // If there were no form errors
        shop.create(createItem.name, createItem.price, createItem.description) match { // Use the shop model to create a new item
          case Some(item) => Ok(views.html.list(shop.list))
          case None => InternalServerError
        }
      }
    )
  }

  /**
    * Create a new item using JSON
    *
    * @return The created item in JSON format upon success, InternalServerError when not succssful
    */
  def createFromJson = Action(parse.json[CreateItem]) { implicit request =>
    shop.create(request.body.name, request.body.price, request.body.description) match {
      case Some(item) => Ok(Json.toJson(item))
      case None => InternalServerError
    }
  }

  /**
    * Display the HTML page contaning the create item form
    *
    * @return The resulting HTML page
    */
  def createForm = Action {
    Ok(views.html.createform(createItemFormModel))
  }

  /**
    * View the details of an item
    *
    * @param id Item ID to view
    * @return The updateItemFormModel with the item details upon success, NotFound when not successful
    */
  def details(id: Long) = Action {
    shop.get(id) match { // Look for item in the shop
      case Some(item) =>
        Ok(views.html.details(item, updateItemFormModel.fill(item))) // Prefill the form with the item details
      case None =>
        NotFound(views.html.notfound("404 - Item not found", s"Item ID: $id"))
    }
  }

  /**
    * View the details of an item in JSON format
    *
    * @param id Item ID to view
    * @return The JSON of the item upon success, NotFound when not successful
    */
  def detailsJson(id: Long) = Action {
    shop.get(id) match {
      case Some(item) =>
        Ok(Json.toJson(item))
      case None =>
        NotFound(views.html.notfound("404 - Item not found", s"Item ID: $id"))
    }
  }

  /**
    * Update / delete an item from the submission of the updateItemFormModel form
    *
    * @return The result of item update / delete, Ok(...) upon success, BadRequest/InternalServerError when not successful
    */
  def updateFromForm = Action(parse.urlFormEncoded) { implicit request =>
    val postAction = request.body.get("action")

    Logger.info(s"[Update Form] Post action: $postAction")

    updateItemFormModel.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.details(Item(-1, "", -1, Some("")), formWithErrors))
      },
      item => {
        postAction.get.head match { // There are two buttons in this form, check which button was used to submit
          case "Delete" => // Delete button
            shop.delete(item.id) match { // Delete the item
              case true => Ok(views.html.list(shop.list))
              case false => InternalServerError
            }
          case "Update" => // Update button
            shop.update(item.id, item.name, item.price, item.description) match { // Update the item
              case Some(item) => Ok(views.html.list(shop.list))
              case None => InternalServerError
            }
        }
      }
    )
  }

  /**
    * Update an item using JSON
    *
    * @param id the ID of the item to update
    * @return The updated item upon success, InternalServerError when not successful
    */
  def update(id: Long) = Action(parse.json[CreateItem]) { implicit request =>
    shop.update(id, request.body.name, request.body.price, request.body.description) match {
      case Some(item) => Ok(Json.toJson(item))
      case None => InternalServerError
    }
  }

  /**
    * Delete an item using JSON
    * @param id the ID of the item to delete
    * @return Ok(...) upon success, InternalServerError when not successful
    */
  def delete(id: Long) = Action { implicit request =>
    shop.delete(id) match {
      case true => Ok("deleted")
      case false => InternalServerError
    }
  }
}
