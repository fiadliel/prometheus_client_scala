package org.lyranthe.prometheus.client

case class RegistryMetric(name: String,
                          labels: List[(String, String)],
                          value: Double)
