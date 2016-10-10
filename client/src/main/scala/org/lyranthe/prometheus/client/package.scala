package org.lyranthe.prometheus

import java.time.Clock

package object client {
  implicit val defaultClock: Clock = Clock.systemUTC()
}
