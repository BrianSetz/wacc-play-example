package models

import java.util.concurrent.atomic.AtomicLong

import scala.collection.concurrent.TrieMap

/**
  * An Item from the shop
  *
  * @param id the ID of the item
  * @param name the name of the item
  * @param price the price of the item
  * @param description the optional description of the item
  */
case class Item(id: Long, name: String, price: Double, description: Option[String])

/**
  * Defines the API of the Shop model, based on CRUD concept
  */
trait Shop {
  def list(): Seq[Item]
  def create(name: String, price: Double, description: Option[String]): Option[Item]
  def get(id: Long): Option[Item]
  def update(id: Long, name: String, price: Double, description: Option[String]): Option[Item]
  def delete(id: Long): Boolean
}

/**
  * Create the Shop model
  */
object Shop extends Shop {
  private val items = TrieMap.empty[Long, Item] // In memory data storage, in reality you would access a database instead !!
  private val seq = new AtomicLong // Mimic auto-increment IDs

  /**
    * List all items in the shop
    *
    * @return A list of all items
    */
  def list(): Seq[Item] = {
    items.values.to[Seq]
  }

  /**
    * Create a new item in the shop
    *
    * @param name the name of the item to create
    * @param price the price of the item to create
    * @param description the optional description of the item to create
    *
    * @return Some(...) created item, or None if failed
    */
  def create(name: String, price: Double, description: Option[String]): Option[Item] = {
    val id = seq.incrementAndGet()
    val item = Item(id, name, price, description)
    items.put(id, item)
    Some(item)
  }

  /**
    * Get an item from the shop
    *
    * @param id the ID of the item to get
    * @return Some(...) created item, or None if failed
    */
  def get(id: Long): Option[Item] = {
    items.get(id)
  }

  /**
    * Update an existing item in the shop
    *
    * @param id the new ID of the item
    * @param name the new name of the item
    * @param price the new price of the item
    * @param description the new optional description of the item
    * @return Some(...) created item, or None if failed
    */
  def update(id: Long, name: String, price: Double, description: Option[String]): Option[Item] = {
    val item = Item(id, name, price, description)
    items.replace(id, item)
    Some(item)
  }

  /**
    * Delete an item from the shop
    *
    * @param id the ID of the item to delete
    * @return True if deletion is successful, false if deletion did not succeed
    */
  def delete(id: Long): Boolean = items.remove(id).isDefined
}