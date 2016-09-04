package io.prometheus.client.scala

case class RegistryMetric(name: String, labels: List[(String, String)], value: Double)
