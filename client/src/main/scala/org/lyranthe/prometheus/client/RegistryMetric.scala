package org.lyranthe.prometheus.client

case class RegistryMetric(suffix: Option[String], labels: List[(String, String)], value: Double)
