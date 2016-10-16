package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.LabelName

case class RegistryMetric(suffix: Option[String], labels: List[(LabelName, String)], value: Double)
