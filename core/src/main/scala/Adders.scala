package io.prometheus.client.scala

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.DoubleAdder

class Adders[A] {
  val adders = new ConcurrentHashMap[A, DoubleAdder]()
  def apply(key: A): DoubleAdder = {
    Option(adders.get(key)) getOrElse {
      adders.putIfAbsent(key, new DoubleAdder)
      adders.get(key)
    }
  }
}

class BucketedAdders[A](numBuckets: Int) {
  val adders = new ConcurrentHashMap[A, Array[DoubleAdder]]
  def apply(key: A): Array[DoubleAdder] = {
    Option(adders.get(key)) getOrElse {
      adders.putIfAbsent(key, Array.fill(numBuckets)(new DoubleAdder))
      adders.get(key)
    }
  }
}

