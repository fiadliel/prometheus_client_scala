package io.prometheus.client.scala

case class RegistryMetric(name: String, labels: Vector[(String, String)], value: Double)
