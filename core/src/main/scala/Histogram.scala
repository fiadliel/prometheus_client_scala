package io.prometheus.client.scala

object Histogram {
  import scala.reflect.macros.whitebox
  import scala.language.experimental.macros

  def create(name: String, labels: String*): Any = macro createHistogramImpl

  def lookup(name: String, labels: String*): Any = macro lookupHistogramImpl

  def createHistogramImpl(c: whitebox.Context)(name: c.Tree, labels: c.Tree*): c.Tree = {
    import c.universe._

    val className = TypeName(s"Histogram${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.histogram._
     new $className[..${List(name) ++ labels}]($name)
    """
  }

  def lookupHistogramImpl(c: whitebox.Context)(name: c.Tree, labels: c.Tree*): c.Tree = {
    import c.universe._

    val className = TypeName(s"Histogram${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.histogram._
     implicitly[$className[..${List(name) ++ labels}]]
    """
  }
}
