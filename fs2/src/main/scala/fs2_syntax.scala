package org.lyranthe.prometheus.client

import java.time.Clock

import fs2._
import fs2.util.{Attempt, Effect}
import fs2.util.syntax._
import internal.counter.LabelledCounter
import internal.gauge.LabelledGauge
import internal.histogram.LabelledHistogram

object fs2_syntax {
  implicit class EffectExtraSyntax[F[_], A](val underlying: F[A]) extends AnyVal {
    def count(f: Attempt[A] => LabelledCounter)(implicit F: Effect[F]): F[A] = {
      underlying.attempt.flatMap {
        case attempt =>
          f(attempt).inc()
          attempt.fold(F.fail, F.pure)
      }
    }

    def countFailure(counter: LabelledCounter)(implicit F: Effect[F]) = {
      underlying.attempt.flatMap {
        _.fold({ t =>
          {
            counter.inc()
            F.fail[A](t)
          }
        }, { result =>
          F.pure(result)
        })
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

    def markSuccess(gauge: LabelledGauge)(implicit F: Effect[F], clock: Clock): F[A] = {
      underlying.map { result =>
        gauge.setToCurrentTime()(clock)
        result
      }
    }

    def timeSuccess(gauge: LabelledGauge)(implicit F: Effect[F]): F[A] = {
      F.delay(System.nanoTime).flatMap { start =>
        underlying.map { result =>
          gauge.set((System.nanoTime - start) / 1e9)
          result
        }
      }
    }

    def time(f: Attempt[A] => LabelledHistogram)(implicit F: Effect[F]): F[A] = {
      F.delay(System.nanoTime).flatMap { start =>
        underlying.attempt.flatMap {
          case attempt =>
            f(attempt).observe((System.nanoTime() - start) / 1e9)
            attempt.fold(F.fail, F.pure)
        }
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
