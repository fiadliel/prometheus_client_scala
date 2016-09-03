package io.prometheus.client.scala

object Counter {
  import scala.reflect.macros.whitebox
  import scala.language.experimental.macros

  def create(name: String, labels: String*): Any = macro createCounterImpl

  def lookup(name: String, labels: String*): Any = macro lookupCounterImpl

  def createCounterImpl(c: whitebox.Context)(name: c.Tree, labels: c.Tree*): c.Tree = {
    import c.universe._

    val className = TypeName(s"Counter${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.counter._
     new $className[..${List(name) ++ labels}]($name)
    """
  }

  def lookupCounterImpl(c: whitebox.Context)(name: c.Tree, labels: c.Tree*): c.Tree = {
    import c.universe._

    val className = TypeName(s"Counter${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.counter._
     implicitly[$className[..${List(name) ++ labels}]]
    """
  }
}
