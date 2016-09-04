package io.prometheus.client.scala

import scala.language.experimental.macros

import io.prometheus.client.scala.internal.Macros

object Gauge {
  def create(name: String)(labels: String*): Any = macro Macros.createGaugeImpl
  def create(name: String, defaultValue: Double)(labels: String*): Any = macro Macros.createGaugeWithDefaultImpl
  def lookup(name: String)(labels: String*): Any = macro Macros.lookupGaugeImpl
}
