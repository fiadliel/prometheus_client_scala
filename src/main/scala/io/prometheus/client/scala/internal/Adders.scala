package io.prometheus.client.scala

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.DoubleAdder

import scala.collection.JavaConverters._

class Adders[A] {
  val adders = new ConcurrentHashMap[A, DoubleAdder]()
  def apply(key: A): DoubleAdder = {
    Option(adders.get(key)) getOrElse {
      adders.putIfAbsent(key, new DoubleAdder)
      adders.get(key)
    }
  }

  def getAll: Iterator[(A, Double)] =
    adders.keys.asScala map { key => key -> adders.get(key).sum() }
}

class BucketedAdders[A](numBuckets: Int) {
  val adders = new ConcurrentHashMap[A, Array[DoubleAdder]]
  def apply(key: A): Array[DoubleAdder] = {
    Option(adders.get(key)) getOrElse {
      adders.putIfAbsent(key, Array.fill(numBuckets)(new DoubleAdder))
      adders.get(key)
    }
  }

  def getAll: Iterator[(A, Array[Double])] =
    adders.keys.asScala map { key => key -> adders.get(key).map(_.sum()) }
}
