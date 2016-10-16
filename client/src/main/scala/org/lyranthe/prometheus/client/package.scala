package org.lyranthe.prometheus

import java.time.Clock

import org.lyranthe.prometheus.client.internal.Macros

import scala.language.experimental.macros

package object client {
  implicit val defaultClock: Clock = Clock.systemUTC()

  implicit class MetricSyntax(sc: StringContext) {
    def metric(pieces: Any*) = macro Macros.verifyPrometheusLabelImpl
  }

}
