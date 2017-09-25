package controllers

import play.api.test.{FakeRequest, WithApplication, PlaySpecification}

class ItemSpec extends PlaySpecification {
  "Item controller" should {
    "list items" in new WithApplication {
      route(app, FakeRequest(controllers.routes.ItemsController.list())) match {
        case Some(response) =>
          status(response) must equalTo (OK)
        case None => failure
      }
    }
  }
}
