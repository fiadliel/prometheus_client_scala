package io.prometheus.client.scala

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.DoubleAdder

import scala.collection.JavaConverters._

class Adders[A](initialValue: Option[Double] = None) {
  val adders = new ConcurrentHashMap[A, DoubleAdder]()
  def apply(key: A): DoubleAdder = {
    Option(adders.get(key)) getOrElse {
      val newAdder = new DoubleAdder
      initialValue.foreach(newAdder.add)
      adders.putIfAbsent(key, newAdder)
      adders.get(key)
    }
  }

  def getAll: List[(A, Double)] =
    adders.keys.asScala map { key => key -> adders.get(key).sum() } toList
}

class BucketedAdders[A](numBuckets: Int, initialValue: Option[Double] = None) {
  val adders = new ConcurrentHashMap[A, Array[DoubleAdder]]
  def apply(key: A): Array[DoubleAdder] = {
    Option(adders.get(key)) getOrElse {
      adders.putIfAbsent(key, Array.fill(numBuckets) {
        val newAdder = new DoubleAdder
        initialValue.foreach(newAdder.add)
        newAdder
      })
      adders.get(key)
    }
  }

  def getAll: List[(A, Array[Double])] =
    adders.keys.asScala map { key => key -> adders.get(key).map(_.sum()) } toList
}
