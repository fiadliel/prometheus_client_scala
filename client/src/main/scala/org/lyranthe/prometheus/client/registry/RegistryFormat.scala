package org.lyranthe.prometheus.client.registry

trait RegistryFormat {
  def contentType: String
  def output(values: => Iterator[RegistryMetrics]): Iterator[Array[Byte]]
}
