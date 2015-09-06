package controllers

import play.api.libs.json.Json
import play.api.test.{FakeRequest, WithApplication, PlaySpecification}

class ItemSpec extends PlaySpecification {
  "Item controller" should {
    "list items" in new WithApplication {
      route(FakeRequest(controllers.routes.Items.list())) match {
        case Some(response) =>
          status(response) must equalTo (OK)
          contentAsJson(response) must equalTo (Json.arr())
        case None => failure
      }
    }
  }
}
