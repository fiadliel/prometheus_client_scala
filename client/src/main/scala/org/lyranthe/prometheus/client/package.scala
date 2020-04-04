package org.lyranthe.prometheus

import java.time.Clock

import org.lyranthe.prometheus.client.internal.Macros

import scala.language.experimental.macros

package object client {
  implicit val defaultClock: Clock = Clock.systemUTC()

  implicit class MetricSyntax(sc: StringContext) {
    def label(pieces: Any*): LabelName = macro Macros.verifyPrometheusLabelImpl
    def metric(pieces: Any*): MetricName =
      macro Macros.verifyPrometheusMetricImpl
  }

}
