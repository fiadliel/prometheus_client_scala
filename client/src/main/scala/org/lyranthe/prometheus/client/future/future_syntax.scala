package org.lyranthe.prometheus.client.future

import org.lyranthe.prometheus.client.counter.LabelledCounter
import org.lyranthe.prometheus.client.histogram.LabelledHistogram

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object future_syntax {

  implicit class MeasurableFuture[T](future: => Future[T]) {

    def count(counter: LabelledCounter)(implicit executionContext: ExecutionContext): Future[T] = {
      future.onComplete {
        case _ => counter.inc()
      }
      future
    }

    def countSuccess(counter: LabelledCounter)(implicit executionContext: ExecutionContext): Future[T] = {
      future.onComplete {
        case Success(_) => counter.inc()
      }
      future
    }

    def countFailure(counter: LabelledCounter)(implicit executionContext: ExecutionContext): Future[T] = {
      future.onComplete {
        case Failure(_) => counter.inc()
      }
      future
    }

    def time(histogram: LabelledHistogram)(implicit executionContext: ExecutionContext): Future[T] = {
      val startTime = System.nanoTime
      future.onComplete {
        case _ => histogram.observe((System.nanoTime() - startTime) / 1e9)
      }
      future
    }

  }


}

