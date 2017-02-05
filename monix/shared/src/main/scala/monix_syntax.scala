package org.lyranthe.prometheus.client

import java.time.Clock

import monix.eval.Task

import scala.util.{Failure, Success, Try}

object monix_syntax {
  implicit class TaskExtraSyntax[A](val underlying: Task[A]) extends AnyVal {
    private def fold[U](value: Try[A])(fa: Throwable => U, fb: A => U): U = {
      value match {
        case Failure(t) => fa(t)
        case Success(v) => fb(v)
      }
    }

    def markSuccess(f: A => LabelledGauge)(implicit clock: Clock): Task[A] = {
      underlying.map { result =>
        f(result).setToCurrentTime()(clock)
        result
      }
    }

    def count(f: Try[A] => Option[(LabelledCounter, Double)]): Task[A] = {
      underlying.materialize.flatMap {
        case attempt =>
          f(attempt).foreach(counter => counter._1.incBy(counter._2))
          underlying
      }
    }

    def countSuccess(f: A => (LabelledCounter, Double)): Task[A] =
      count(fold(_)(_ => None, f.andThen(Some(_))))

    def countFailure(f: Throwable => LabelledCounter): Task[A] =
      count(fold(_)(f.andThen(v => Some((v, 1D))), _ => None))

    def time(f: Try[A] => Option[LabelledHistogram]): Task[A] = {
      Task.eval(Timer()).flatMap { timer =>
        underlying.materialize.flatMap {
          case attempt =>
            f(attempt).foreach(_.observeDuration(timer))
            underlying
        }
      }
    }

    def timeSuccess(f: A => LabelledHistogram): Task[A] =
      time(fold(_)(_ => None, f.andThen(Some(_))))

    def timeFailure(f: Throwable => LabelledHistogram): Task[A] =
      time(fold(_)(f.andThen(Some(_)), _ => None))
  }
}
