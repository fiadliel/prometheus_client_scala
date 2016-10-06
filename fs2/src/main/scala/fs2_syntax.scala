package org.lyranthe.prometheus.client.scala

import fs2._
import fs2.util.Suspendable
import fs2.util.syntax._
import org.lyranthe.prometheus.client.scala.internal.counter.LabelledCounter
import org.lyranthe.prometheus.client.scala.internal.histogram.LabelledHistogram

object fs2_syntax {
  implicit class SuspendableExtraSyntax[F[_], A](val underlying: F[A])
      extends AnyVal {
    def countSuccess(counter: LabelledCounter)(
        implicit F: Suspendable[F]): F[A] = {
      F.delay(System.nanoTime).flatMap { start =>
        underlying.map { result =>
          counter.inc()
          result
        }
      }
    }
    def timeSuccess(histogram: LabelledHistogram)(
        implicit F: Suspendable[F]): F[A] = {
      F.delay(System.nanoTime).flatMap { start =>
        underlying.map { result =>
          histogram.observe((System.nanoTime - start) / 1e9)
          result
        }
      }
    }
  }
}

//object fs2_syntax {
//
//  implicit class SuspendableExtraSyntax[F[_], A](val underlying: F[A])
//      extends AnyVal {
//    def countSuccess(successCounter: Counter)(
//        implicit F: Suspendable[F]): F[A] = {
//      underlying.map { result =>
//        successCounter.inc()
//        result
//      }
//    }
//
//    def countFailure(failureCounter: Counter)(implicit F: Suspendable[F],
//                                              C: Catchable[F]): F[A] = {
//      underlying.attempt.flatMap {
//        case l @ Left(t) =>
//          failureCounter.inc()
//          C.fail(t)
//        case r @ Right(value) =>
//          C.pure(value)
//      }
//    }
//
//    def count(successCounter: Counter, failureCounter: Counter)(
//        implicit F: Suspendable[F],
//        C: Catchable[F]): F[A] = {
//      underlying.attempt.flatMap {
//        case l @ Left(t) =>
//          failureCounter.inc()
//          C.fail(t)
//        case r @ Right(value) =>
//          successCounter.inc()
//          C.pure(value)
//      }
//    }
//
//    def timeSuccess(successGauge: Gauge)(implicit F: Suspendable[F]): F[A] = {
//      F.delay(System.nanoTime()).flatMap { start =>
//        underlying.map { result =>
//          successGauge.set((System.nanoTime() - start) / 1e9)
//          result
//        }
//      }
//    }
//
//    def timeFailure(failureGauge: Gauge)(implicit F: Suspendable[F],
//                                         C: Catchable[F]): F[A] = {
//      F.delay(System.nanoTime()).flatMap { start =>
//        underlying.attempt.flatMap {
//          case l @ Left(t) =>
//            failureGauge.set((System.nanoTime() - start) / 1e9)
//            C.fail(t)
//          case r @ Right(value) =>
//            C.pure(value)
//        }
//      }
//    }
//
//    def time(successGauge: Gauge, failureGauge: Gauge)(
//        implicit F: Suspendable[F],
//        C: Catchable[F]): F[A] = {
//      F.delay(System.nanoTime()).flatMap { start =>
//        underlying.attempt.flatMap {
//          case l @ Left(t) =>
//            failureGauge.set((System.nanoTime() - start) / 1e9)
//            C.fail(t)
//          case r @ Right(value) =>
//            successGauge.set((System.nanoTime() - start) / 1e9)
//            C.pure(value)
//        }
//      }
//    }
//
//    def timeSuccess(successHistogram: Histogram)(
//        implicit F: Suspendable[F]): F[A] = {
//      F.delay(System.nanoTime()).flatMap { start =>
//        underlying.map { result =>
//          successHistogram.observe((System.nanoTime() - start) / 1e9)
//          result
//        }
//      }
//    }
//
//    def timeFailure(failureHistogram: Histogram)(implicit F: Suspendable[F],
//                                                 C: Catchable[F]): F[A] = {
//      F.delay(System.nanoTime()).flatMap { start =>
//        underlying.attempt.flatMap {
//          case l @ Left(t) =>
//            failureHistogram.observe((System.nanoTime() - start) / 1e9)
//            C.fail(t)
//          case r @ Right(value) =>
//            C.pure(value)
//        }
//      }
//    }
//
//    def time(successHistogram: Histogram, failureHistogram: Histogram)(
//        implicit F: Suspendable[F],
//        C: Catchable[F]): F[A] = {
//      F.delay(System.nanoTime()).flatMap { start =>
//        underlying.attempt.flatMap {
//          case l @ Left(t) =>
//            failureHistogram.observe((System.nanoTime() - start) / 1e9)
//            C.fail(t)
//          case r @ Right(value) =>
//            successHistogram.observe((System.nanoTime() - start) / 1e9)
//            C.pure(value)
//        }
//      }
//    }
//
//  }
//
//}
