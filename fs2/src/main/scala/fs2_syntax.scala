//package io.prometheus.client.scala
//
//import fs2.util.{Catchable, Suspendable}
//import fs2.util.syntax._
//import io.prometheus.client.scala.internal.counter.{Counter, Counter0}
//import io.prometheus.client.scala.internal.gauge.Gauge
//import io.prometheus.client.scala.internal.histogram.Histogram
//
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
