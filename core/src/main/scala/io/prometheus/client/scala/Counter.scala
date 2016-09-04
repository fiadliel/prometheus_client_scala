package io.prometheus.client.scala

import scala.language.experimental.macros

import io.prometheus.client.scala.internal.Macros

object Counter {
  def create(name: String)(labels: String*): Any = macro Macros.createCounterImpl
  def lookup(name: String)(labels: String*): Any = macro Macros.lookupCounterImpl
}
