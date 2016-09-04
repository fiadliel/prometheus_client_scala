package io.prometheus.client.scala

import scala.language.experimental.macros

import io.prometheus.client.scala.internal.Macros

object Histogram {
  def create(name: String, buckets: Seq[Double])(labels: String*): Any = macro Macros.createHistogramImpl
  def lookup(name: String)(labels: String*): Any = macro Macros.lookupHistogramImpl
}
