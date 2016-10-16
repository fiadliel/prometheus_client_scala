package org.lyranthe.prometheus.client.internal

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object Macros {
  private val PrometheusMetricFormat = """^[a-zA-Z_:][a-zA-Z0-9_:]*$""".r
  private val PrometheusLabelFormat = """^[a-zA-Z_][a-zA-Z0-9_]*$""".r

  def verifyPrometheusMetricImpl(c: Context)(pieces: c.Expr[Any]*): c.Expr[MetricName] = {
    import c.universe._

    c.prefix.tree match {
      case Apply(_, List(Apply(_, rawParts))) =>
        val fullMetric: StringBuffer = new StringBuffer

        rawParts zipAll (pieces map (_.tree), reify("").tree, reify("").tree) map {
          case (Literal(Constant(rawPart: String)), Literal(Constant(piece))) =>
            fullMetric append rawPart
            fullMetric append piece.toString

          case (Literal(Constant(rawPart: String)), piece) =>
            c.abort(piece.pos, "Non-literal value supplied")
        }

        val result = fullMetric.toString

        PrometheusMetricFormat.findFirstIn(result) match {
          case None =>
            c.abort(c.enclosingPosition,
                    s"Metric format incorrect: $result, should follow format ${PrometheusMetricFormat.regex}")
          case Some(_) =>
            c.Expr(q"""_root_.org.lyranthe.prometheus.client.internal.MetricName(${result})""")
        }
    }
  }

  def verifyPrometheusLabelImpl(c: Context)(pieces: c.Expr[Any]*): c.Expr[LabelName] = {
    import c.universe._

    c.prefix.tree match {
      case Apply(_, List(Apply(_, rawParts))) =>
        val fullMetric: StringBuffer = new StringBuffer

        rawParts zipAll (pieces map (_.tree), reify("").tree, reify("").tree) map {
          case (Literal(Constant(rawPart: String)), Literal(Constant(piece))) =>
            fullMetric append rawPart
            fullMetric append piece.toString

          case (Literal(Constant(rawPart: String)), piece) =>
            c.abort(piece.pos, "Non-literal value supplied")
        }

        val result = fullMetric.toString

        PrometheusLabelFormat.findFirstIn(result) match {
          case None =>
            c.abort(c.enclosingPosition,
              s"Metric format incorrect: $result, should follow format ${PrometheusLabelFormat.regex}")
          case Some(_) =>
            c.Expr(q"""_root_.org.lyranthe.prometheus.client.internal.LabelName(${result})""")
        }
    }
  }

}
