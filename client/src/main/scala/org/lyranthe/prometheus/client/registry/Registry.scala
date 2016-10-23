package org.lyranthe.prometheus.client.registry

trait Registry {
  def unsafeRegister(c: MetricFamily): Unit
  def collect(): Iterator[RegistryMetrics]

  def output[A](format: RegistryFormat.Aux[A]): Iterator[A] =
    format.output(collect)
}
