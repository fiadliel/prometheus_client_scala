package org.lyranthe.prometheus.client.scala

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.DoubleAdder

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

trait Adder {
  def add(value: Double): Unit
  def sum(): Double
}

class UnsynchronizedAdder(underlying: DoubleAdder = new DoubleAdder)
    extends Adder {
  def add(value: Double): Unit =
    underlying.add(value)

  def sum(): Double = {
    underlying.sum()
  }
}

class SynchronizedAdder(underlying: DoubleAdder = new DoubleAdder)
    extends Adder {
  def add(value: Double): Unit =
    underlying.add(value)

  def set(value: Double): Unit =
    synchronized {
      underlying.reset()
      underlying.add(value)
    }

  def sum(): Double = {
    synchronized(underlying.sum())
  }
}

class Adders[A, B <: Adder](init: => B, initialValue: Option[Double] = None) {
  val adders = new ConcurrentHashMap[A, B]()
  def apply(key: A): B = {
    Option(adders.get(key)) getOrElse {
      val newAdder = init
      initialValue.foreach(newAdder.add)
      adders.putIfAbsent(key, newAdder)
      adders.get(key)
    }
  }

  def remove(key: A): Unit = {
    adders.remove(key)
  }

  def clear(): Unit =
    adders.clear()

  def getAll: List[(A, Double)] =
    adders.keys.asScala map { key =>
      key -> adders.get(key).sum()
    } toList
}

class BucketedAdders[A, B <: Adder: ClassTag](init: => B,
                                              numBuckets: Int,
                                              initialValue: Option[Double] =
                                                None) {
  val adders = new ConcurrentHashMap[A, Array[B]]
  def apply(key: A): Array[B] = {
    Option(adders.get(key)) getOrElse {
      adders.putIfAbsent(key, Array.fill(numBuckets) {
        val newAdder = init
        initialValue.foreach(newAdder.add)
        newAdder
      })
      adders.get(key)
    }
  }

  def remove(key: A): Unit = {
    adders.remove(key)
  }

  def clear(): Unit =
    adders.clear()

  def getAll: List[(A, Array[Double])] =
    adders.keys.asScala map { key =>
      key -> adders.get(key).map(_.sum())
    } toList
}
