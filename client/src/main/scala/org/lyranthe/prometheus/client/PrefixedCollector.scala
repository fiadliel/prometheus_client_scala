package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.Collector

trait PrefixedCollector extends Collector {
  def name: String

  final def underlyingName: Option[String] = Some(name)
}
