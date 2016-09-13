package models

import org.specs2.mutable.Specification

class ShopSpec extends Specification {
  "A shop" should {
    "add items" in {
      Shop.create("Test Item", 42, Some("Description")) must beSome[Item].which {
        item =>
          item.name == "Test Item" &&
          item.price == 42 &&
          item.description.contains("Description")
      }
    }
  }
}
