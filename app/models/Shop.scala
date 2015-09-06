package models

import java.util.concurrent.atomic.AtomicLong

import scala.collection.concurrent.TrieMap

case class Item(id: Long, name: String, price: Double, description: Option[String])

trait Shop {
  def list(): Seq[Item]
  def create(name: String, price: Double, description: Option[String]): Option[Item]
  def get(id: Long): Option[Item]
  def update(id: Long, name: String, price: Double, description: Option[String]): Option[Item]
  def delete(id: Long): Boolean
}

object Shop extends Shop {
  private val items = TrieMap.empty[Long, Item]
  private val seq = new AtomicLong

  def list: Seq[Item] = {
    items.values.to[Seq]
  }

  def create(name: String, price: Double, description: Option[String]): Option[Item] = {
    val id = seq.incrementAndGet()
    val item = Item(id, name, price, description)
    items.put(id, item)
    Some(item)
  }

  def get(id: Long): Option[Item] = {
    items.get(id)
  }

  def update(id: Long, name: String, price: Double, description: Option[String]): Option[Item] = {
    val item = Item(id, name, price, description)
    items.replace(id, item)
    Some(item)
  }

  def delete(id: Long): Boolean = items.remove(id).isDefined
}