package org.lyranthe.prometheus.client.scala

case class RegistryMetric(name: String,
                          labels: List[(String, String)],
                          value: Double)
