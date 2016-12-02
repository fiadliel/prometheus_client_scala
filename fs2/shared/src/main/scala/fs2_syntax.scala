package org.lyranthe.prometheus.client

import java.time.Clock

import fs2._
import fs2.util.{Attempt, Effect}
import fs2.util.syntax._

object fs2_syntax {
  implicit class EffectExtraSyntax[F[_], A](val underlying: F[A])
      extends AnyVal {
    def markSuccess(f: A => LabelledGauge)(implicit F: Effect[F],
                                           clock: Clock): F[A] = {
      underlying.map { result =>
        f(result).setToCurrentTime()(clock)
        result
      }
    }

    def count(f: Attempt[A] => Option[(LabelledCounter, Double)])(
        implicit F: Effect[F]): F[A] = {
      underlying.attempt.flatMap {
        case attempt =>
          f(attempt).foreach(counter => counter._1.incBy(counter._2))
          attempt.fold(F.fail, F.pure)
      }
    }

    def countSuccess(f: A => (LabelledCounter, Double))(
        implicit F: Effect[F]): F[A] =
      count(_.fold(_ => None, f.andThen(Some(_))))

    def countFailure(f: Throwable => LabelledCounter)(
        implicit F: Effect[F]): F[A] =
      count(_.fold(f.andThen(v => Some((v, 1D))), _ => None))

    def time(f: Attempt[A] => Option[LabelledHistogram])(
        implicit F: Effect[F]): F[A] = {
      F.delay(Timer()).flatMap { timer =>
        underlying.attempt.flatMap {
          case attempt =>
            f(attempt).foreach(_.observeDuration(timer))
            attempt.fold(F.fail, F.pure)
        }
      }
    }

    def timeSuccess(f: A => LabelledHistogram)(implicit F: Effect[F]): F[A] =
      time(_.fold(_ => None, f.andThen(Some(_))))

    def timeFailure(f: Throwable => LabelledHistogram)(
        implicit F: Effect[F]): F[A] =
      time(_.fold(f.andThen(Some(_)), _ => None))
  }
}
