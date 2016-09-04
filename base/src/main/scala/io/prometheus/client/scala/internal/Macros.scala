package io.prometheus.client.scala.internal

import scala.reflect.macros.whitebox

object Macros {
  def createCounterImpl(c: scala.reflect.macros.whitebox.Context)(name: c.Tree)(labels: c.Tree*): c.Tree = {
    import c.universe._

    name match {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
        c.enclosingPosition,
        "Must provide a String constant for the name."
      )
    }

    labels foreach {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for labels."
        )
    }

    val className = TypeName(s"Counter${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.counter._
     new $className[..${List(name) ++ labels}]($name)(..$labels)
    """
  }

  def lookupCounterImpl(c: scala.reflect.macros.whitebox.Context)(name: c.Tree)(labels: c.Tree*): c.Tree = {
    import c.universe._

    name match {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for the name."
        )
    }

    labels foreach {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for labels."
        )
    }

    val className = TypeName(s"Counter${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.counter._
     implicitly[$className[..${List(name) ++ labels}]]
    """
  }

  def createGaugeImpl(c: whitebox.Context)(name: c.Tree)(labels: c.Tree*): c.Tree = {
    import c.universe._

    name match {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for the name."
        )
    }

    labels foreach {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for labels."
        )
    }

    val className = TypeName(s"Gauge${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.gauge._
     new $className[..${List(name) ++ labels}]($name)(..$labels)
    """
  }

  def createGaugeWithDefaultImpl(c: whitebox.Context)(name: c.Tree, defaultValue: c.Tree)(labels: c.Tree*): c.Tree = {
    import c.universe._

    name match {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for the name."
        )
    }

    labels foreach {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for labels."
        )
    }

    val className = TypeName(s"Gauge${labels.size}")
    val default = q"Some($defaultValue)"

    q"""
     import _root_.io.prometheus.client.scala.internal.gauge._
     new $className[..${List(name) ++ labels}](..${List(name, default)})(..$labels)
    """
  }

  def lookupGaugeImpl(c: whitebox.Context)(name: c.Tree)(labels: c.Tree*): c.Tree = {
    import c.universe._

    name match {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for the name."
        )
    }

    labels foreach {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for labels."
        )
    }

    val className = TypeName(s"Gauge${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.gauge._
     implicitly[$className[..${List(name) ++ labels}]]
    """
  }

  def createHistogramImpl(c: whitebox.Context)(name: c.Tree, buckets: c.Tree)(labels: c.Tree*): c.Tree = {
    import c.universe._

    name match {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for the name."
        )
    }

    labels foreach {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for labels."
        )
    }

    val className = TypeName(s"Histogram${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.histogram._
     new $className[..${List(name) ++ labels}](..${List(name, buckets)})(..$labels)
    """
  }

  def lookupHistogramImpl(c: whitebox.Context)(name: c.Tree)(labels: c.Tree*): c.Tree = {
    import c.universe._

    name match {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for the name."
        )
    }

    labels foreach {
      case Literal(Constant(ct)) =>
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Must provide a String constant for labels."
        )
    }

    val className = TypeName(s"Histogram${labels.size}")

    q"""
     import _root_.io.prometheus.client.scala.internal.histogram._
     implicitly[$className[..${List(name) ++ labels}]]
    """
  }
}
