# Introduction to Prometheus Scala client

## Creating and finding monitoring variables

This scala client makes extensive use of the scala type system when
creating monitoring collectors. Every collector has a distinctive
type, based on the collector name, and any labels attached.

You can therefore use the implicit scope to store and lookup
monitoring collectors.

### Creating a collector

Here is an example where a simple counter is created:

```scala
scala> import io.prometheus.client.scala._
import io.prometheus.client.scala._

scala> val totalRequests = Counter("total_requests", "Total requests").labels()
totalRequests: io.prometheus.client.scala.internal.counter.Counter0 = Counter0(total_requests)()
```

Note that the counter is a `Counter0`, which means that it
has no labels. Therefore, it needs no corresponding label values
when incrementing the counter.

You can use this counter:

```scala
scala> totalRequests.inc()
```

### Creating collectors with labels

Any extra strings passed when creating a collector, represent
labels for any monitoring variables. Whenever any information is
passed to the monitoring system, an appropriate number of label
values need to be provided; one for each label.

```scala
scala> val totalErrors = Counter("total_errors", "Total errors").labels("code")
totalErrors: io.prometheus.client.scala.internal.counter.Counter1 = Counter1(total_errors)(code)
```

### Using collectors with labels

To increment a counter with an error code of "404", one might
do the following:

```scala
scala> totalErrors.labelValues("404").inc()
```

This is supported up to 22 labels, for example:

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
lotsOfLabels: io.prometheus.client.scala.internal.counter.Counter22 = Counter22(lots_of_labels)(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22)
```

We will obviously get a compilation error if we try to provide an incorrect
number of values when using this collector:

```scala
scala> lotsOfLabels.labelValues("1val", "2val").inc()
<console>:17: error: not enough arguments for method labelValues: (labelValue1: String, labelValue2: String, labelValue3: String, labelValue4: String, labelValue5: String, labelValue6: String, labelValue7: String, labelValue8: String, labelValue9: String, labelValue10: String, labelValue11: String, labelValue12: String, labelValue13: String, labelValue14: String, labelValue15: String, labelValue16: String, labelValue17: String, labelValue18: String, labelValue19: String, labelValue20: String, labelValue21: String, labelValue22: String)io.prometheus.client.scala.internal.counter.LabelledCounter.
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
histogramBuckets: io.prometheus.client.scala.HistogramBuckets{val buckets: List[Double]} = io.prometheus.client.scala.HistogramBuckets$$anon$1@6a356e8b

scala> val activeRequests = Gauge("active_requests", "Active requests").labels().register
activeRequests: io.prometheus.client.scala.internal.gauge.Gauge0 = Gauge0(active_requests)()

scala> val numErrors = Counter("num_errors", "Total errors").labels().register
numErrors: io.prometheus.client.scala.internal.counter.Counter0 = Counter0(num_errors)()

scala> val requestLatency = Histogram("request_latency", "Request latency").labels("path").register
requestLatency: io.prometheus.client.scala.internal.histogram.Histogram1 = Histogram1(request_latency, List(1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0, Infinity))(path)

scala> activeRequests.set(50)

scala> numErrors.inc

scala> requestLatency.labelValues("/home").observe(17)

scala> implicitly[Registry].collect
res6: List[io.prometheus.client.scala.RegistryMetric] = List(RegistryMetric(active_requests,List(),50.0), RegistryMetric(num_errors,List(),1.0), RegistryMetric(request_latency_total,List((path,/home)),17.0), RegistryMetric(request_latency_sum,List((path,/home)),1.0), RegistryMetric(request_latency_bucket,List((le,1.0), (path,/home)),0.0), RegistryMetric(request_latency_bucket,List((le,2.0), (path,/home)),0.0), RegistryMetric(request_latency_bucket,List((le,5.0), (path,/home)),0.0), RegistryMetric(request_latency_bucket,List((le,10.0), (path,/home)),0.0), RegistryMetric(request_latency_bucket,List((le,20.0), (path,/home)),1.0), RegistryMetric(request_latency_bucket,List((le,50.0), (path,/home)),1.0), RegistryMetric(request_latency_bucket,List((le,100.0), (path,/home)),1.0), RegistryMetri...
```

## Using with FS2 Task (WIP)

Both gauges and histograms can be used to time FS2 Tasks (or any type which has an `fs2.util.Suspendable` instance).

Certain imports are needed:

```scala
import io.prometheus.client.scala.fs2_syntax._
import fs2._
```

Then the method `timeEffect` can be used to capture the duration of the task (in seconds):

```scala
implicit val requestLatency = Histogram.create("request_latency", Seq(0.02, 0.05, 0.1, 0.2, 0.5, 1.0))()
val mySleepyTask = Task.delay(Thread.sleep(scala.util.Random.nextInt(1200)))
val myTimedSleepyTask = Histogram.lookup("request_latency")().timeEffect(mySleepyTask)

for (i <- Range(1, 10)) myTimedSleepyTask.unsafeRun

Histogram.lookup("request_latency")().collect
```
