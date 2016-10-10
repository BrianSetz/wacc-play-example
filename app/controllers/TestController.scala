package controllers

import javax.inject.Inject

import scala.concurrent.Future
import play.api.Logger
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.Random

// BSON-JSON conversions/collection
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

// Reactive Mongo imports
import reactivemongo.api.Cursor

import play.modules.reactivemongo.{// ReactiveMongo Play2 plugin
MongoController,
ReactiveMongoApi,
ReactiveMongoComponents
}


/*
 * Example using ReactiveMongo + Play JSON library.
 *
 * There are two approaches demonstrated in this controller:
 * - using JsObjects directly
 * - using case classes that can be turned into JSON using Reads and Writes.
 *
 * This controller uses JsObjects directly.
 *
 * Instead of using the default Collection implementation (which interacts with
 * BSON structures + BSONReader/BSONWriter), we use a specialized
 * implementation that works with JsObject + Reads/Writes.
 *
 * Of course, you can still use the default Collection implementation
 * (BSONCollection.) See ReactiveMongo examples to learn how to use it.
 */
class TestController @Inject()(val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  /*
   * Get a JSONCollection (a Collection implementation that is designed to work
   * with JsObject, Reads and Writes.)
   * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
   * the collection reference to avoid potential problems in development with
   * Play hot-reloading.
   */
  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("persons"))

  def index = Action {
    Ok("works")
  }

  def createRandom = Action.async {
    val json = Json.obj(
      "name" -> Random.alphanumeric.take(10).mkString,
      "age" -> Random.nextInt(100),
      "created" -> new java.util.Date().getTime())

    collection.flatMap(_.insert(json).map(lastError =>
      Ok("Mongo LastError: %s".format(lastError))))
  }

  def create(name: String, age: Int) = Action.async {
    val json = Json.obj(
      "name" -> name,
      "age" -> age,
      "created" -> new java.util.Date().getTime())

    collection.flatMap(_.insert(json).map(lastError =>
      Ok("Mongo LastError: %s".format(lastError))))
  }

  def createFromJson = Action.async(parse.json) { request =>
    import play.api.libs.json.Reads._
    /*
     * request.body is a JsValue.
     * There is an implicit Writes that turns this JsValue as a JsObject,
     * so you can call insert() with this JsValue.
     * (insert() takes a JsObject as parameter, or anything that can be
     * turned into a JsObject using a Writes.)
     */
    val transformer: Reads[JsObject] =
    Reads.jsPickBranch[JsString](__ \ "firstName") and
      Reads.jsPickBranch[JsString](__ \ "lastName") and
      Reads.jsPickBranch[JsNumber](__ \ "age") reduce

    request.body.transform(transformer).map { result =>
      collection.flatMap(_.insert(result).map { lastError =>
        Logger.debug(s"Successfully inserted with LastError: $lastError")
        Created
      })
    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def findByName(name: String) = Action.async {
    // let's do our query
    val futurePersonsList: Future[List[JsObject]] = collection.flatMap(_.
      // find all people with name `name`
      find(Json.obj("name" -> name)).
      // sort them by creation date
      sort(Json.obj("created" -> -1)).
      // perform the query and get a cursor of JsObject
      cursor[JsObject]().collect[List]())

    // transform the list into a JsArray
    val futurePersonsJsonArray: Future[JsArray] =
    futurePersonsList.map { persons => Json.arr(persons) }

    // everything's ok! Let's reply with the array
    futurePersonsJsonArray.map { persons =>
      Ok(persons)
    }
  }

  def findAll = Action.async {
    // let's do our query
    val futurePersonsList: Future[List[JsObject]] = collection.flatMap(_.
      find(Json.obj()).
      sort(Json.obj("created" -> -1)).
      cursor[JsObject]().collect[List]())

    val futurePersonsJsonArray: Future[JsArray] =
    futurePersonsList.map { persons => Json.arr(persons) }

    futurePersonsJsonArray.map { persons =>
      Ok(persons)
    }
  }
}