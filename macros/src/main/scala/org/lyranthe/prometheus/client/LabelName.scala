package org.lyranthe.prometheus.client

import scala.util.matching.Regex

case class LabelName(name: String) extends AnyVal

object LabelName {
  val PrometheusLabelFormat: Regex = """^[a-zA-Z_][a-zA-Z0-9_]*$""".r

  def unsafeFromString(s: String): LabelName = {
    PrometheusLabelFormat.findFirstIn(s) match {
      case None =>
        throw new IllegalArgumentException(s"$s does not match required label format ${PrometheusLabelFormat.regex}")
      case Some(formatted) =>
        LabelName(formatted)
    }
  }
}
