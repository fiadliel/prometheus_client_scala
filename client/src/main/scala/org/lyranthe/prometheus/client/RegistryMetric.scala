package org.lyranthe.prometheus.client

case class RegistryMetric(suffix: Option[String], labels: List[(LabelName, String)], value: Double)
