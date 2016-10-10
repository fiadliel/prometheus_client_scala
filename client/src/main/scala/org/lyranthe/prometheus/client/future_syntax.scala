package org.lyranthe.prometheus.client

import java.time.Clock

import org.lyranthe.prometheus.client.internal.counter.LabelledCounter
import org.lyranthe.prometheus.client.internal.gauge.LabelledGauge
import org.lyranthe.prometheus.client.internal.histogram.LabelledHistogram

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object future_syntax {
  implicit class FutureExtraSyntax[A](val underlying: Future[A]) extends AnyVal {
    def count(f: Future[A] => LabelledCounter)(implicit ec: ExecutionContext): Future[A] = {
      f(underlying).inc()
      underlying
    }

    def countFailure(counter: LabelledCounter)(implicit ec: ExecutionContext) = {
      underlying.onFailure {
        case t =>
          counter.inc()
      }
    }

    def countSuccess(f: A => (LabelledCounter, Double))(implicit ec: ExecutionContext) = {
      underlying.onSuccess {
        case result =>
          val (counter, incBy) = f(result)
          counter.incBy(incBy)
      }
    }

    def countSuccess(counter: LabelledCounter)(implicit ec: ExecutionContext) = {
      underlying.map { result =>
        counter.inc()
        result
      }
    }

    def markSuccess(gauge: LabelledGauge)(implicit ec: ExecutionContext, clock: Clock): Future[A] = {
      underlying.onSuccess { case _ => gauge.setToCurrentTime() }
      underlying
    }

    def time(f: Try[A] => LabelledHistogram)(implicit ec: ExecutionContext): Future[A] = {
      val start = System.nanoTime()
      underlying.onComplete {
        case v => f(v).observe((System.nanoTime() - start) / 1e9)
      }
      underlying
    }

    def timeSuccess(gauge: LabelledGauge)(implicit ec: ExecutionContext): Future[A] = {
      val start = System.nanoTime()
      underlying.onSuccess {
        case _ => gauge.set((System.nanoTime() - start) / 1e9)
      }
      underlying
    }

    def timeSuccess(f: A => LabelledHistogram)(implicit ec: ExecutionContext): Future[A] = {
      val start = System.nanoTime()
      underlying.onSuccess {
        case v => f(v).observe((System.nanoTime() - start) / 1e9)
      }
      underlying
    }

    def timeSuccess(histogram: LabelledHistogram)(implicit ec: ExecutionContext): Future[A] = {
      val start = System.nanoTime()
      underlying.onSuccess {
        case _ => histogram.observe((System.nanoTime() - start) / 1e9)
      }
      underlying
    }
  }
}
