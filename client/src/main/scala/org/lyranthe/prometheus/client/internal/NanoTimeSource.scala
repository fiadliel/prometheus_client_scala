package org.lyranthe.prometheus.client.internal

trait NanoTimeSource {
  def nanoTime: Long
}

object SystemNanoTimeSource extends NanoTimeSource {
  def nanoTime(): Long = System.nanoTime()
}

object NanoTimeSource {
  implicit val defaultNanoTimeSource: NanoTimeSource = SystemNanoTimeSource
}
