package io.prometheus.client.scala

object Histogram {
  import scala.language.experimental.macros
  import scala.reflect.macros.whitebox

  def create(name: String, buckets: Seq[Double])(labels: String*): Any = macro createHistogramImpl

  def lookup(name: String)(labels: String*): Any = macro lookupHistogramImpl

  def createHistogramImpl(c: whitebox.Context)(name: c.Tree, buckets: c.Tree)(labels: c.Tree*): c.Tree = {
    import c.universe._

    val className = TypeName(s"Histogram${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.histogram._
     new $className[..${List(name) ++ labels}](..${List(name, buckets)})(..$labels)
    """
  }

  def lookupHistogramImpl(c: whitebox.Context)(name: c.Tree)(labels: c.Tree*): c.Tree = {
    import c.universe._

    val className = TypeName(s"Histogram${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.histogram._
     implicitly[$className[..${List(name) ++ labels}]]
    """
  }
}
