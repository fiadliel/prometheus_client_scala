package org.lyranthe.prometheus.client.internal

import org.lyranthe.prometheus.client.{LabelName, MetricName}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object Macros {
  def verifyPrometheusMetricImpl(c: Context)(
      pieces: c.Expr[Any]*): c.Expr[MetricName] = {
    import c.universe._

    c.prefix.tree match {
      case Apply(_, List(Apply(_, rawParts))) =>
        val fullMetric: StringBuffer = new StringBuffer

        rawParts zipAll (pieces map (_.tree), reify("").tree, reify("").tree) map {
          case (Literal(Constant(rawPart: String)),
                Literal(Constant(piece))) =>
            fullMetric append rawPart
            fullMetric append piece.toString

          case (Literal(Constant(rawPart: String)), piece) =>
            c.abort(piece.pos, "Non-literal value supplied")
        }

        val result = fullMetric.toString

        MetricName.PrometheusMetricFormat.findFirstIn(result) match {
          case None =>
            c.abort(
              c.enclosingPosition,
              s"Metric format incorrect: $result, should follow format ${MetricName.PrometheusMetricFormat.regex}")
          case Some(_) =>
            c.Expr(
              q"""_root_.org.lyranthe.prometheus.client.MetricName(${result})""")
        }
    }
  }

  def verifyPrometheusLabelImpl(c: Context)(
      pieces: c.Expr[Any]*): c.Expr[LabelName] = {
    import c.universe._

    c.prefix.tree match {
      case Apply(_, List(Apply(_, rawParts))) =>
        val fullMetric: StringBuffer = new StringBuffer

        rawParts zipAll (pieces map (_.tree), reify("").tree, reify("").tree) map {
          case (Literal(Constant(rawPart: String)),
                Literal(Constant(piece))) =>
            fullMetric append rawPart
            fullMetric append piece.toString

          case (Literal(Constant(rawPart: String)), piece) =>
            c.abort(piece.pos, "Non-literal value supplied")
        }

        val result = fullMetric.toString

        if (result.startsWith("__"))
          c.abort(
            c.enclosingPosition,
            s"""Label format incorrect: $result, labels beginning with "__" are reserved""")

        LabelName.PrometheusLabelFormat.findFirstIn(result) match {
          case None =>
            c.abort(
              c.enclosingPosition,
              s"Label format incorrect: $result, should follow format ${LabelName.PrometheusLabelFormat.regex}")
          case Some(_) =>
            c.Expr(
              q"""_root_.org.lyranthe.prometheus.client.LabelName(${result})""")
        }
    }
  }

}
