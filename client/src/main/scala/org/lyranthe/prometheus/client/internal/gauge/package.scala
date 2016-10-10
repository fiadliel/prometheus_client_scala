package org.lyranthe.prometheus.client.internal

import java.time.Clock

package object gauge {
  implicit val defaultClock: Clock = Clock.systemUTC()
}
