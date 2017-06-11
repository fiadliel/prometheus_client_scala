package org.lyranthe.prometheus.client

import java.time.Clock

import cats.implicits._
import cats.effect.Sync

object cats_syntax {
  implicit class SyncExtraSyntax[F[_], A](val underlying: F[A])
      extends AnyVal {
    def markSuccess(f: A => LabelledGauge)(implicit F: Sync[F],
                                           clock: Clock): F[A] = {
      underlying.map { result =>
        f(result).setToCurrentTime()(clock)
        result
      }
    }

    def count(f: Either[Throwable, A] => Option[(LabelledCounter, Double)])(
        implicit F: Sync[F]): F[A] = {
      underlying.attempt.flatMap {
        case attempt =>
          f(attempt).foreach(counter => counter._1.incBy(counter._2))
          attempt.fold(F.raiseError, F.pure)
      }
    }

    def countSuccess(f: A => (LabelledCounter, Double))(
        implicit F: Sync[F]): F[A] =
      count(_.fold(_ => None, f.andThen(Some(_))))

    def countFailure(f: Throwable => LabelledCounter)(
        implicit F: Sync[F]): F[A] =
      count(_.fold(f.andThen(v => Some((v, 1D))), _ => None))

    def time(f: Either[Throwable, A] => Option[LabelledHistogram])(
        implicit F: Sync[F]): F[A] = {
      F.delay(Timer()).flatMap { timer =>
        underlying.attempt.flatMap {
          case attempt =>
            f(attempt).foreach(_.observeDuration(timer))
            attempt.fold(F.raiseError, F.pure)
        }
      }
    }

    def timeSuccess(f: A => LabelledHistogram)(implicit F: Sync[F]): F[A] =
      time(_.fold(_ => None, f.andThen(Some(_))))

    def timeFailure(f: Throwable => LabelledHistogram)(
        implicit F: Sync[F]): F[A] =
      time(_.fold(f.andThen(Some(_)), _ => None))
  }
}
