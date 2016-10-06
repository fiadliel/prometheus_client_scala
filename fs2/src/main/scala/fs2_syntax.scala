package org.lyranthe.prometheus.client.scala

import fs2._
import fs2.util.Effect
import fs2.util.syntax._
import org.lyranthe.prometheus.client.scala.internal.counter.LabelledCounter
import org.lyranthe.prometheus.client.scala.internal.histogram.LabelledHistogram

object fs2_syntax {
  implicit class EffectExtraSyntax[F[_], A](val underlying: F[A]) extends AnyVal {
    def count(f: F[A] => LabelledCounter)(implicit F: Effect[F]): F[A] = {
      f(underlying).inc()
      underlying
    }

    def countFailure(counter: LabelledCounter)(implicit F: Effect[F]) = {
      underlying.attempt.map {
        case l @ Left(t) =>
          counter.inc()
          throw t
        case Right(result) =>
          result
      }
    }

    def countSuccess(f: A => (LabelledCounter, Double))(implicit F: Effect[F]) = {
      underlying.map { result =>
        val (counter, incBy) = f(result)
        counter.incBy(incBy)
        result
      }
    }

    def countSuccess(counter: LabelledCounter)(implicit F: Effect[F]): F[A] = {
      underlying.map { result =>
        counter.inc()
        result
      }
    }

    def time(f: F[A] => LabelledHistogram)(implicit F: Effect[F]): F[A] = {
      F.delay(System.nanoTime).flatMap { start =>
        val histogram = f(underlying)
        histogram.observe((System.nanoTime - start) / 1e9)
        underlying
      }
    }

    def timeSuccess(f: A => LabelledHistogram)(implicit F: Effect[F]): F[A] = {
      F.delay(System.nanoTime).flatMap { start =>
        underlying.map { result =>
          f(result).observe((System.nanoTime - start) / 1e9)
          result
        }
      }
    }

    def timeSuccess(histogram: LabelledHistogram)(implicit F: Effect[F]): F[A] = {
      F.delay(System.nanoTime).flatMap { start =>
        underlying.map { result =>
          histogram.observe((System.nanoTime - start) / 1e9)
          result
        }
      }
    }
  }
}
