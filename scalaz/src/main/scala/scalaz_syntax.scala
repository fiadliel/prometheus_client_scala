package org.lyranthe.prometheus.client

import java.time.Clock

import scalaz.\/
import scalaz.concurrent.Task

object scalaz_syntax {
  implicit class TaskExtraSyntax[A](val underlying: Task[A])
      extends AnyVal {
    def markSuccess(f: A => LabelledGauge)(implicit clock: Clock): Task[A] = {
      underlying.map { result =>
        f(result).setToCurrentTime()(clock)
        result
      }
    }

    def count(f: Throwable \/ A => Option[(LabelledCounter, Double)]): Task[A] = {
      underlying.attempt.flatMap {
        case attempt =>
          f(attempt).foreach(counter => counter._1.incBy(counter._2))
          attempt.fold(Task.fail, Task.now)
      }
    }

    def countSuccess(f: A => (LabelledCounter, Double)): Task[A] =
      count(_.fold(_ => None, f.andThen(Some(_))))

    def countFailure(f: Throwable => LabelledCounter): Task[A] =
      count(_.fold(f.andThen(v => Some((v, 1D))), _ => None))

    def time(f: Throwable \/ A => Option[LabelledHistogram]): Task[A] = {
      Task.delay(Timer()).flatMap { timer =>
        underlying.attempt.flatMap {
          case attempt =>
            f(attempt).foreach(_.observeDuration(timer))
            attempt.fold(Task.fail, Task.now)
        }
      }
    }

    def timeSuccess(f: A => LabelledHistogram): Task[A] =
      time(_.fold(_ => None, f.andThen(Some(_))))

    def timeFailure(f: Throwable => LabelledHistogram): Task[A] =
      time(_.fold(f.andThen(Some(_)), _ => None))
  }
}
