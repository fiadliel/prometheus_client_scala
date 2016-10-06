# Introduction to Prometheus Scala client

## Creating monitoring variables

Here is an example where a simple counter is created:

```scala
scala> import org.lyranthe.prometheus.client.scala._
import org.lyranthe.prometheus.client.scala._

scala> val totalRequests = Counter("total_requests", "Total requests").labels()
totalRequests: org.lyranthe.prometheus.client.scala.internal.counter.Counter0 = Counter0(total_requests)()
```

Note that the counter is a `Counter0`, which means that it
has no labels. Therefore, it needs no corresponding label values
when incrementing the counter.

You can use this counter:

```scala
scala> totalRequests.inc()
```

If you need labels attached to the counter, specify the label names using
the `.labels` method:

```scala
scala> val totalErrors = Counter("total_errors", "Total errors").labels("code")
totalErrors: org.lyranthe.prometheus.client.scala.internal.counter.Counter1 = Counter1(total_errors)(code)
```

### Using counters

To increment a counter with an error code of "404", one might
do the following:

```scala
scala> totalErrors.labelValues("404").inc()
```

This is supported for up to 22 labels, for example:

```scala
scala> val lotsOfLabels =
     |   Counter("lots_of_labels", "Lots of labels").labels(
     |     "1",
     |     "2",
     |     "3",
     |     "4",
     |     "5",
     |     "6",
     |     "7",
     |     "8",
     |     "9",
     |     "10",
     |     "11",
     |     "12",
     |     "13",
     |     "14",
     |     "15",
     |     "16",
     |     "17",
     |     "18",
     |     "19",
     |     "20",
     |     "21",
     |     "22"
     |   )
lotsOfLabels: org.lyranthe.prometheus.client.scala.internal.counter.Counter22 = Counter22(lots_of_labels)(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22)
```

We will obviously get a compilation error if we try to provide an incorrect
number of values when using this counter:

```scala
scala> lotsOfLabels.labelValues("1val", "2val").inc()
<console>:17: error: not enough arguments for method labelValues: (labelValue1: String, labelValue2: String, labelValue3: String, labelValue4: String, labelValue5: String, labelValue6: String, labelValue7: String, labelValue8: String, labelValue9: String, labelValue10: String, labelValue11: String, labelValue12: String, labelValue13: String, labelValue14: String, labelValue15: String, labelValue16: String, labelValue17: String, labelValue18: String, labelValue19: String, labelValue20: String, labelValue21: String, labelValue22: String)org.lyranthe.prometheus.client.scala.internal.counter.LabelledCounter.
Unspecified value parameters labelValue3, labelValue4, labelValue5...
       lotsOfLabels.labelValues("1val", "2val").inc()
                               ^
```

## The Registry

The `Registry` is a service to which collectors are registered. You can
then call `collect` to obtain current monitoring values for all the
registered collectors.

There is a default registry available, which is used if no other registry
is specified.

```scala
scala> implicit val histogramBuckets = HistogramBuckets(1, 2, 5, 10, 20, 50, 100)
histogramBuckets: org.lyranthe.prometheus.client.scala.HistogramBuckets{val buckets: List[Double]} = HistogramBuckets(1.0,2.0,5.0,10.0,20.0,50.0,100.0,Infinity)

scala> val activeRequests = Gauge("active_requests", "Active requests").labels().register
activeRequests: org.lyranthe.prometheus.client.scala.internal.gauge.Gauge0 = Gauge0(active_requests)()

scala> val numErrors = Counter("num_errors", "Total errors").labels().register
numErrors: org.lyranthe.prometheus.client.scala.internal.counter.Counter0 = Counter0(num_errors)()

scala> val requestLatency = Histogram("request_latency", "Request latency").labels("path").register
requestLatency: org.lyranthe.prometheus.client.scala.internal.histogram.Histogram1 = Histogram1(request_latency, List(1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0, Infinity))(path)

scala> activeRequests.set(50)

scala> numErrors.inc

scala> requestLatency.labelValues("/home").observe(17)

scala> implicitly[Registry]
res6: org.lyranthe.prometheus.client.scala.Registry =
active_requests 50.0
num_errors 1.0
request_latency_total{path="/home"} 17.0
request_latency_sum{path="/home"} 1.0
request_latency_bucket{le="1.0",path="/home"} 0.0
request_latency_bucket{le="2.0",path="/home"} 0.0
request_latency_bucket{le="5.0",path="/home"} 0.0
request_latency_bucket{le="10.0",path="/home"} 0.0
request_latency_bucket{le="20.0",path="/home"} 1.0
request_latency_bucket{le="50.0",path="/home"} 1.0
request_latency_bucket{le="100.0",path="/home"} 1.0
request_latency_bucket{le="+Inf",path="/home"} 1.0
```

## Using with FS2 Task (WIP)

Both gauges and histograms can be used to time FS2 Tasks (or any type which has an `fs2.util.Effect` instance).

Certain imports are needed:

```scala
scala> import fs2._
import fs2._

scala> import org.lyranthe.prometheus.client.scala._
import org.lyranthe.prometheus.client.scala._

scala> import org.lyranthe.prometheus.client.scala.fs2_syntax._
import org.lyranthe.prometheus.client.scala.fs2_syntax._
```

Then the method `timeSuccess` can be used to capture the duration of the task (in seconds):

```scala
implicit val registry = new org.lyranthe.prometheus.client.scala.internal.DefaultRegistry
```
```scala
scala> implicit val histogramBuckets = HistogramBuckets(0.02, 0.05, 0.1, 0.2, 0.5, 1.0)
histogramBuckets: org.lyranthe.prometheus.client.scala.HistogramBuckets{val buckets: List[Double]} = HistogramBuckets(0.02,0.05,0.1,0.2,0.5,1.0,Infinity)

scala> val requestLatency = Histogram("request_latency", "Request latency").labels("path").register
requestLatency: org.lyranthe.prometheus.client.scala.internal.histogram.Histogram1 = Histogram1(request_latency, List(0.02, 0.05, 0.1, 0.2, 0.5, 1.0, Infinity))(path)

scala> val mySleepyTask = Task.delay(Thread.sleep(scala.util.Random.nextInt(800)))
mySleepyTask: fs2.Task[Unit] = Task

scala> val myTimedSleepyTask = mySleepyTask.timeSuccess(requestLatency.labelValues("/home"))
myTimedSleepyTask: fs2.Task[Unit] = Task

scala> for (i <- Range(1, 10)) myTimedSleepyTask.unsafeRun

scala> implicitly[Registry]
res1: org.lyranthe.prometheus.client.scala.Registry =
request_latency_total{path="/home"} 3.3619944540000004
request_latency_sum{path="/home"} 9.0
request_latency_bucket{le="0.02",path="/home"} 1.0
request_latency_bucket{le="0.05",path="/home"} 2.0
request_latency_bucket{le="0.1",path="/home"} 3.0
request_latency_bucket{le="0.2",path="/home"} 3.0
request_latency_bucket{le="0.5",path="/home"} 6.0
request_latency_bucket{le="1.0",path="/home"} 9.0
request_latency_bucket{le="+Inf",path="/home"} 9.0
```
