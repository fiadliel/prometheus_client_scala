package org.lyranthe.prometheus.client.internal

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{DoubleAdder, LongAdder}

import scala.collection.JavaConverters._

private[client] trait Adder[AdderType <: AnyVal] {
  def add(value: AdderType): Unit
  def sum(): AdderType
}

private[client] class UnsynchronizedDoubleAdder(
    underlying: DoubleAdder = new DoubleAdder)
    extends Adder[Double] {
  def add(value: Double): Unit =
    underlying.add(value)

  def sum(): Double = {
    underlying.sum()
  }
}

private[client] class UnsynchronizedLongAdder(
    underlying: LongAdder = new LongAdder)
    extends Adder[Long] {
  def add(value: Long): Unit =
    underlying.add(value)

  def sum(): Long = {
    underlying.sum()
  }
}

private[client] class SynchronizedDoubleAdder(
    underlying: DoubleAdder = new DoubleAdder)
    extends Adder[Double] {
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

private[client] class Adders[A, B <: Adder[Double]](
    init: => B,
    initialValue: Option[Double] = None) {
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

private[client] class BucketedAdders[A](bucketValues: Array[Double]) {
  val adders =
    new ConcurrentHashMap[A,
                          (UnsynchronizedDoubleAdder,
                           Array[(Double, UnsynchronizedLongAdder)])]

  def apply(key: A)
    : (UnsynchronizedDoubleAdder, Array[(Double, UnsynchronizedLongAdder)]) = {
    val value = adders.get(key)
    if (value != null)
      value
    else {
      adders.putIfAbsent(key,
                         (new UnsynchronizedDoubleAdder,
                          bucketValues.map(_ -> new UnsynchronizedLongAdder)))
      adders.get(key)
    }
  }

  def remove(key: A): Unit = {
    adders.remove(key)
  }

  def clear(): Unit =
    adders.clear()

  def getAll: List[(A, (Double, Array[(Double, Long)]))] =
    adders.keys.asScala map { key =>
      key -> {
        val idx = adders.get(key)
        (idx._1.sum, idx._2.map(v => v._1 -> v._2.sum))
      }
    } toList
}
