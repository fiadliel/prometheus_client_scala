# Introduction to Prometheus Scala client

## Synopsis

Prometheus offers an idiomatic API for instrumenting applications written in Scala. It tries to provide an API which is efficient, easy to use. 
Also, as far as possible, it tries to report API usage errors at compile time instead of runtime.
 
In addition, it offers some extra insight into your program by exposing some useful VM statistics.

## Important definitions

#### Metric

A metric is a particular "thing" you wish to monitor over time. It is represented in one of three ways:
 - gauges, which provide a single value which may go up or down
 - counters, which increase monotonically
 - histograms, which provide values aggregated into configured buckets.

#### Label

Metrics may have associated labels. For each supplied label, a corresponding label value
is provided when noting an event. These labels are useful for grouping your timeseries
in various ways.

## Getting started

To start using the API, import the main package:

```scala
scala> import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client._
```

## String interpolators

Metric names and labels have certain requirements for what characters are allowed. To
allow this to be correctly checked at compile time, two string interpolators are provided:

 - `metric""` creates metric names
 - `label""` creates label names

These interpolators require constant values, some examples are shown below.

Some valid metric names:

```scala
scala> metric"http_requests_total"
res0: org.lyranthe.prometheus.client.MetricName = MetricName(http_requests_total)

scala> final val system = "http"
system: String("http") = http

scala> final val subsystem = "requests"
subsystem: String("requests") = requests

scala> metric"${system}_${subsystem}_total"
res1: org.lyranthe.prometheus.client.MetricName = MetricName(http_requests_total)
```

Some invalid metric names:

```scala
scala> metric"1"
<console>:16: error: Metric format incorrect: 1, should follow format ^[a-zA-Z_:][a-zA-Z0-9_:]*$
       metric"1"
       ^
```

```scala
scala> val subsystem = "requests"
subsystem: String = requests
```
```scala
scala> metric"${system}_${subsystem}_total"
<console>:18: error: Non-literal value supplied
       metric"${system}_${subsystem}_total"
                          ^
```

### Creating a monitoring variable

To create a simple counter:

```scala
scala> val totalRequests = Counter(metric"total_requests", "Total requests").labels()
totalRequests: org.lyranthe.prometheus.client.counter.Counter0 = Counter(MetricName(total_requests))()
```

The resulting counter has a metric name `total_requests`, a help message with the contents,
"Total requests", and has no labels.

You can use this in this way:

```scala
scala> totalRequests.inc()
```

### Creating a monitoring variable with labels

If you need labels attached to the counter, specify the label names using
the `.labels` method:

```scala
scala> val totalErrors = Counter(metric"total_errors", "Total errors").labels(label"code")
totalErrors: org.lyranthe.prometheus.client.counter.Counter1 = Counter1(MetricName(total_errors),Total errors,List(LabelName(code)))
```

To increment a counter with an error code of "404", one might
do the following:

```scala
scala> totalErrors.labelValues("404").inc()
```

### Behavior with incorrect input

We will obviously get a compilation error if we try to provide an incorrect
number of values when using this counter:

```scala
scala> totalErrors.labelValues("404", "/path").inc()
<console>:17: error: too many arguments (2) for method labelValues: (labelValue1: String)org.lyranthe.prometheus.client.counter.LabelledCounter
       totalErrors.labelValues("404", "/path").inc()
                                      ^
```

## The Registry

The `Registry` is a service to which collectors are registered. You can
then call `collect` to obtain current monitoring values for all the
registered collectors.

You can create a registry with a default implementation with:

```scala
scala> implicit val defaultRegistry = DefaultRegistry()
defaultRegistry: org.lyranthe.prometheus.client.DefaultRegistry = org.lyranthe.prometheus.client.DefaultRegistry@2de9d563
```

```scala
scala> implicit val histogramBuckets = HistogramBuckets(1, 2, 5, 10, 20, 50, 100)
histogramBuckets: org.lyranthe.prometheus.client.HistogramBuckets{val buckets: List[Double]} = HistogramBuckets(1.0,2.0,5.0,10.0,20.0,50.0,100.0,Infinity)

scala> val activeRequests = Gauge(metric"active_requests", "Active requests").labels().register
activeRequests: org.lyranthe.prometheus.client.gauge.Gauge0 = Counter(MetricName(active_requests))()

scala> val numErrors = Counter(metric"num_errors", "Total errors").labels().register
numErrors: org.lyranthe.prometheus.client.counter.Counter0 = Counter(MetricName(num_errors))()

scala> val requestLatency = Histogram(metric"request_latency", "Request latency").labels(label"path").register
requestLatency: org.lyranthe.prometheus.client.histogram.Histogram1 = Histogram1(MetricName(request_latency),Request latency,List(LabelName(path)),List(1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0, Infinity))

scala> activeRequests.set(50)

scala> numErrors.inc

scala> requestLatency.labelValues("/home").observe(17)

scala> implicitly[Registry].outputText
res10: String =
"# HELP active_requests Active requests
# TYPE active_requests gauge
active_requests 50.0
# HELP num_errors Total errors
# TYPE num_errors counter
num_errors 1.0
# HELP request_latency Request latency
# TYPE request_latency histogram
request_latency_bucket{le="1.0",path="/home"} 0
request_latency_bucket{le="2.0",path="/home"} 0
request_latency_bucket{le="5.0",path="/home"} 0
request_latency_bucket{le="10.0",path="/home"} 0
request_latency_bucket{le="20.0",path="/home"} 1
request_latency_bucket{le="50.0",path="/home"} 1
request_latency_bucket{le="100.0",path="/home"} 1
request_latency_bucket{le="+Inf",path="/home"} 1
request_latency_count{path="/home"} 1
request_latency_sum{path="/home"} 17.0
"
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
implicit val defaultRegistry = DefaultRegistry()
```
```scala
scala> implicit val histogramBuckets = HistogramBuckets(0.02, 0.05, 0.1, 0.2, 0.5, 1.0)
histogramBuckets: org.lyranthe.prometheus.client.HistogramBuckets{val buckets: List[Double]} = HistogramBuckets(0.02,0.05,0.1,0.2,0.5,1.0,Infinity)

scala> val requestLatency = Histogram(metric"request_latency", "Request latency").labels(label"path").register
requestLatency: org.lyranthe.prometheus.client.histogram.Histogram1 = Histogram1(MetricName(request_latency),Request latency,List(LabelName(path)),List(0.02, 0.05, 0.1, 0.2, 0.5, 1.0, Infinity))

scala> val mySleepyTask = Task.delay(Thread.sleep(scala.util.Random.nextInt(800)))
mySleepyTask: fs2.Task[Unit] = Task

scala> val myTimedSleepyTask = mySleepyTask.timeSuccess(requestLatency.labelValues("/home"))
myTimedSleepyTask: fs2.Task[Unit] = Task

scala> for (i <- Range(1, 10)) myTimedSleepyTask.unsafeRun

scala> implicitly[Registry].outputText
res1: String =
"# HELP request_latency Request latency
# TYPE request_latency histogram
request_latency_bucket{le="0.02",path="/home"} 1
request_latency_bucket{le="0.05",path="/home"} 2
request_latency_bucket{le="0.1",path="/home"} 2
request_latency_bucket{le="0.2",path="/home"} 3
request_latency_bucket{le="0.5",path="/home"} 6
request_latency_bucket{le="1.0",path="/home"} 9
request_latency_bucket{le="+Inf",path="/home"} 9
request_latency_count{path="/home"} 9
request_latency_sum{path="/home"} 3.1627661240000005
"
```

## Exposing JMX Statistics

Some JVM statistics can be exposed with:

```scala
implicit val defaultRegistry = DefaultRegistry()
```
```scala
scala> import fs2._
import fs2._

scala> import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client._

scala> jmx.register
res2: Boolean = false

scala> println(implicitly[Registry].outputText)
# HELP jvm_classloader JVM Classloader statistics
# TYPE jvm_classloader gauge
jvm_classloader{classloader="loaded"} 15222.0
jvm_classloader{classloader="total-loaded"} 15301.0
jvm_classloader{classloader="unloaded"} 79.0
# HELP jvm_gc_stats JVM Garbage Collector Statistics
# TYPE jvm_gc_stats gauge
jvm_gc_stats{name="PS Scavenge",type="count"} 11.0
jvm_gc_stats{name="PS Scavenge",type="time"} 0.319
jvm_gc_stats{name="PS MarkSweep",type="count"} 5.0
jvm_gc_stats{name="PS MarkSweep",type="time"} 0.326
# HELP jvm_memory_usage JVM Memory Usage
# TYPE jvm_memory_usage gauge
jvm_memory_usage{region="heap",type="committed"} 9.98244352E8
jvm_memory_usage{region="heap",type="init"} 5.36870912E8
jvm_memory_usage{region="heap",type="max"} 1.431830528E9
jvm_memory_usage{region="heap",type="used"} 2.71817368E8
jvm_memory_usage{region="non-heap",type="committed"} 1.90070784E8
jvm_memory_usage{region="non-heap",type="init"} 2555904.0
jvm_memory_usage{region="non-heap",type="max"} -1.0
jvm_memory_usage{region="non-heap",type="used"} 1.81783664E8
# HELP jvm_start_time JVM Start Time
# TYPE jvm_start_time gauge
jvm_start_time 1.478810439329E9
# HELP jvm_threads JVM Thread Information
# TYPE jvm_threads gauge
jvm_threads{type="non-daemon"} 12.0
jvm_threads{type="daemon"} 4.0

```
