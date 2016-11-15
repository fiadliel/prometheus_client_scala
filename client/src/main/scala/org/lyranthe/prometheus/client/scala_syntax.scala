package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.counter.LabelledCounter
import org.lyranthe.prometheus.client.histogram.LabelledHistogram

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object scala_syntax {

  object helper {

    def all[T, U](metric: U): Try[T] => Option[U] = {
      _ => Some(metric)
    }

    def success[T, U](metric: U): Try[T] => Option[U] = {
      case Success(_) => Some(metric)
      case Failure(_) => None
    }

    def failure[T, U](metric: U): Try[T] => Option[U] = {
      case Success(_) => None
      case Failure(_) => Some(metric)
    }

  }

  def count[T](counter: LabelledCounter)(t: => Try[T]): Try[T] = {
    countN(t)(helper.all(counter))
  }

  def countSuccess[T](counter: LabelledCounter)(t: => Try[T]): Try[T] = {
    countN(t)(helper.success(counter))
  }

  def countFailure[T](counter: LabelledCounter)(t: => Try[T]): Try[T] = {
    countN(t)(helper.failure(counter))
  }

  def countN[T](t: => Try[T])(f: (Try[T] => Option[LabelledCounter])*): Try[T] = {
    val attempt = t
    f.foreach(_.apply(attempt).foreach(_.inc()))
    attempt
  }

  def time[T](histogram: LabelledHistogram)(t: => Try[T]): Try[T] = {
    timeN(t)(helper.all(histogram))(Timer())
  }

  def timeSuccess[T](histogram: LabelledHistogram)(t: => Try[T]): Try[T] = {
    timeN(t)(helper.success(histogram))(Timer())
  }

  def timeFailure[T](histogram: LabelledHistogram)(t: => Try[T]): Try[T] = {
    timeN(t)(helper.failure(histogram))(Timer())
  }

  def timeN[T](t: => Try[T])(f: => Try[T] => Option[LabelledHistogram])(timer: Timer): Try[T] = {
    val attempt = t
    f.apply(attempt).foreach(_.observeDuration(timer))
    attempt
  }

  def timeFuture[T](histogram: LabelledHistogram)(future: => Future[T])(implicit executionContext: ExecutionContext): Future[T] = {
    timeFutureN(future)(helper.all(histogram))(Timer())
  }

  def timeFutureSuccess[T](histogram: LabelledHistogram)(future: => Future[T])(implicit executionContext: ExecutionContext): Future[T] = {
    timeFutureN(future)(helper.success(histogram))(Timer())
  }

  def timeFutureFailure[T](histogram: LabelledHistogram)(future: => Future[T])(implicit executionContext: ExecutionContext): Future[T] = {
    timeFutureN(future)(helper.failure(histogram))(Timer())
  }

  def timeFutureN[T](fut: => Future[T])(f: (Try[T] => Option[LabelledHistogram])*)(timer: Timer)(implicit executionContext: ExecutionContext): Future[T] = {
    val future = fut
    future.onComplete(result => f.foreach(_.apply(result).foreach(_.observeDuration(timer))))
    future
  }

}

