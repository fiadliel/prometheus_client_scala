package io.prometheus.client.scala

object Gauge {
  import scala.language.experimental.macros
  import scala.reflect.macros.whitebox

  def create(name: String, labels: String*): Any = macro createGaugeImpl

  def lookup(name: String, labels: String*): Any = macro lookupGaugeImpl

  def createGaugeImpl(c: whitebox.Context)(name: c.Tree, labels: c.Tree*): c.Tree = {
    import c.universe._

    val className = TypeName(s"Gauge${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.gauge._
     new $className[..${List(name) ++ labels}](..${List(name) ++ labels})
    """
  }

  def lookupGaugeImpl(c: whitebox.Context)(name: c.Tree, labels: c.Tree*): c.Tree = {
    import c.universe._

    val className = TypeName(s"Gauge${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.gauge._
     implicitly[$className[..${List(name) ++ labels}]]
    """
  }
}
