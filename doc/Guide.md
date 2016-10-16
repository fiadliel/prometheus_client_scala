# Introduction to Prometheus Scala client

## Creating monitoring variables

Here is an example where a simple counter is created:

```scala
scala> import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client._

scala> val totalRequests = Counter(metric"total_requests", "Total requests").labels()
totalRequests: org.lyranthe.prometheus.client.internal.counter.Counter0 = Counter(MetricName(total_requests))()
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
scala> val totalErrors = Counter(metric"total_errors", "Total errors").labels(label"code")
totalErrors: org.lyranthe.prometheus.client.internal.counter.Counter1 = Counter1(MetricName(total_errors),Total errors,List(LabelName(code)))
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
     |   Counter(metric"lots_of_labels", "Lots of labels").labels(
     |     label"l1",
     |     label"l2",
     |     label"l3",
     |     label"l4",
     |     label"l5",
     |     label"l6",
     |     label"l7",
     |     label"l8",
     |     label"l9",
     |     label"l10",
     |     label"l11",
     |     label"l12",
     |     label"l13",
     |     label"l14",
     |     label"l15",
     |     label"l16",
     |     label"l17",
     |     label"l18",
     |     label"l19",
     |     label"l20",
     |     label"l21",
     |     label"l22"
     |   )
lotsOfLabels: org.lyranthe.prometheus.client.internal.counter.Counter22 = Counter22(MetricName(lots_of_labels),Lots of labels,List(LabelName(l1), LabelName(l2), LabelName(l3), LabelName(l4), LabelName(l5), LabelName(l6), LabelName(l7), LabelName(l8), LabelName(l9), LabelName(l10), LabelName(l11), LabelName(l12), LabelName(l13), LabelName(l14), LabelName(l15), LabelName(l16), LabelName(l17), LabelName(l18), LabelName(l19), LabelName(l20), LabelName(l21), LabelName(l22)))
```

We will obviously get a compilation error if we try to provide an incorrect
number of values when using this counter:

```scala
scala> lotsOfLabels.labelValues("1val", "2val").inc()
<console>:17: error: not enough arguments for method labelValues: (labelValue1: String, labelValue2: String, labelValue3: String, labelValue4: String, labelValue5: String, labelValue6: String, labelValue7: String, labelValue8: String, labelValue9: String, labelValue10: String, labelValue11: String, labelValue12: String, labelValue13: String, labelValue14: String, labelValue15: String, labelValue16: String, labelValue17: String, labelValue18: String, labelValue19: String, labelValue20: String, labelValue21: String, labelValue22: String)org.lyranthe.prometheus.client.internal.counter.LabelledCounter.
Unspecified value parameters labelValue3, labelValue4, labelValue5...
       lotsOfLabels.labelValues("1val", "2val").inc()
                               ^
```

## The Registry

The `Registry` is a service to which collectors are registered. You can
then call `collect` to obtain current monitoring values for all the
registered collectors.

You can create a registry with a default implementation with:

```scala
scala> implicit val defaultRegistry = DefaultRegistry()
defaultRegistry: org.lyranthe.prometheus.client.DefaultRegistry =
```

```scala
scala> implicit val histogramBuckets = HistogramBuckets(1, 2, 5, 10, 20, 50, 100)
histogramBuckets: org.lyranthe.prometheus.client.HistogramBuckets{val buckets: List[Double]} = HistogramBuckets(1.0,2.0,5.0,10.0,20.0,50.0,100.0,Infinity)

scala> val activeRequests = Gauge(metric"active_requests", "Active requests").labels().unsafeRegister
activeRequests: org.lyranthe.prometheus.client.internal.gauge.Gauge0 = Counter(MetricName(active_requests))()

scala> val numErrors = Counter(metric"num_errors", "Total errors").labels().unsafeRegister
numErrors: org.lyranthe.prometheus.client.internal.counter.Counter0 = Counter(MetricName(num_errors))()

scala> val requestLatency = Histogram(metric"request_latency", "Request latency").labels(label"path").unsafeRegister
requestLatency: org.lyranthe.prometheus.client.internal.histogram.Histogram1 = Histogram1(MetricName(request_latency),Request latency,List(LabelName(path)),List((1.0,0), (2.0,1), (5.0,2), (10.0,3), (20.0,4), (50.0,5), (100.0,6), (Infinity,7)))

scala> activeRequests.set(50)

scala> numErrors.inc

scala> requestLatency.labelValues("/home").observe(17)

scala> implicitly[Registry]
res6: org.lyranthe.prometheus.client.Registry =
# HELP request_latency Request latency
# TYPE request_latency histogram
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
# HELP num_errors Total errors
# TYPE num_errors counter
num_errors 1.0
# HELP active_requests Active requests
# TYPE active_requests gauge
active_requests 50.0
```

## Using with FS2 Task (WIP)

Both gauges and histograms can be used to time FS2 Tasks (or any type which has an `fs2.util.Effect` instance).

Certain imports are needed:

```scala
scala> import fs2._
import fs2._

scala> import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client._

scala> import org.lyranthe.prometheus.client.fs2_syntax._
import org.lyranthe.prometheus.client.fs2_syntax._
```

Then the method `timeSuccess` can be used to capture the duration of the task (in seconds):

```scala
implicit val registry = DefaultRegistry()
```
```scala
scala> implicit val histogramBuckets = HistogramBuckets(0.02, 0.05, 0.1, 0.2, 0.5, 1.0)
histogramBuckets: org.lyranthe.prometheus.client.HistogramBuckets{val buckets: List[Double]} = HistogramBuckets(0.02,0.05,0.1,0.2,0.5,1.0,Infinity)

scala> val requestLatency = Histogram(metric"request_latency", "Request latency").labels(label"path").unsafeRegister
requestLatency: org.lyranthe.prometheus.client.internal.histogram.Histogram1 = Histogram1(MetricName(request_latency),Request latency,List(LabelName(path)),List((0.02,0), (0.05,1), (0.1,2), (0.2,3), (0.5,4), (1.0,5), (Infinity,6)))

scala> val mySleepyTask = Task.delay(Thread.sleep(scala.util.Random.nextInt(800)))
mySleepyTask: fs2.Task[Unit] = Task

scala> val myTimedSleepyTask = mySleepyTask.timeSuccess(requestLatency.labelValues("/home"))
myTimedSleepyTask: fs2.Task[Unit] = Task

scala> for (i <- Range(1, 10)) myTimedSleepyTask.unsafeRun

scala> implicitly[Registry]
res1: org.lyranthe.prometheus.client.Registry =
# HELP request_latency Request latency
# TYPE request_latency histogram
request_latency_total{path="/home"} 4.439022873000001
request_latency_sum{path="/home"} 9.0
request_latency_bucket{le="0.02",path="/home"} 1.0
request_latency_bucket{le="0.05",path="/home"} 1.0
request_latency_bucket{le="0.1",path="/home"} 1.0
request_latency_bucket{le="0.2",path="/home"} 1.0
request_latency_bucket{le="0.5",path="/home"} 3.0
request_latency_bucket{le="1.0",path="/home"} 9.0
request_latency_bucket{le="+Inf",path="/home"} 9.0
```

## Exposing JMX Statistics

Some JVM statistics can be exposed with:

```scala
implicit val registry = DefaultRegistry()
```
```scala
scala> import fs2._
import fs2._

scala> import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client._

scala> jmx.unsafeRegister

scala> println(implicitly[Registry])
# HELP jvm_threads JVM Thread Information
# TYPE jvm_threads gauge
jvm_threads{type="non-daemon"} 11.0
jvm_threads{type="daemon"} 4.0
# HELP jvm_start_time JVM Start Time
# TYPE jvm_start_time gauge
jvm_start_time 1.476639807551E9
# HELP jvm_memory_usage JVM Memory Usage
# TYPE jvm_memory_usage gauge
jvm_memory_usage{region="heap",type="committed"} 1.11673344E9
jvm_memory_usage{region="heap",type="init"} 5.36870912E8
jvm_memory_usage{region="heap",type="max"} 1.908932608E9
jvm_memory_usage{region="heap",type="used"} 3.5407168E8
jvm_memory_usage{region="non-heap",type="committed"} 1.48062208E8
jvm_memory_usage{region="non-heap",type="init"} 2555904.0
jvm_memory_usage{region="non-heap",type="max"} -1.0
jvm_memory_usage{region="non-heap",type="used"} 1.46109448E8
# HELP jvm_gc_stats JVM Garbage Collector Statistics
# TYPE jvm_gc_stats gauge
jvm_gc_stats{name="PS Scavenge",type="count"} 8.0
jvm_gc_stats{name="PS Scavenge",type="time"} 0.15
jvm_gc_stats{name="PS MarkSweep",type="count"} 5.0
jvm_gc_stats{name="PS MarkSweep",type="time"} 0.354
# HELP jvm_classloader JVM Classloader statistics
# TYPE jvm_classloader gauge
jvm_classloader{classloader="loaded"} 15199.0
jvm_classloader{classloader="total-loaded"} 15273.0
jvm_classloader{classloader="unloaded"} 74.0

```
