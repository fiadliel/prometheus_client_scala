package org.lyranthe.prometheus.client.registry

trait RegistryFormat {
  type Out
  def output(values: => Iterator[RegistryMetrics]): Iterator[Out]
}

object RegistryFormat {
  type Aux[Out0] = RegistryFormat { type Out = Out0 }
}
