# Introduction to Prometheus Scala client

## Creating monitoring variables

Here is an example where a simple counter is created:

```tut
import org.lyranthe.prometheus.client._

val totalRequests = Counter(metric"total_requests", "Total requests").labels()
```

Note that the counter is a `Counter0`, which means that it
has no labels. Therefore, it needs no corresponding label values
when incrementing the counter.

You can use this counter:

```tut
totalRequests.inc()
```

If you need labels attached to the counter, specify the label names using
the `.labels` method:

```tut
val totalErrors = Counter(metric"total_errors", "Total errors").labels(label"code")
```

### Using counters

To increment a counter with an error code of "404", one might
do the following:

```tut
totalErrors.labelValues("404").inc()
```

This is supported for up to 22 labels, for example:

```tut
val lotsOfLabels =
  Counter(metric"lots_of_labels", "Lots of labels").labels(
    label"l1",
    label"l2",
    label"l3",
    label"l4",
    label"l5",
    label"l6",
    label"l7",
    label"l8",
    label"l9",
    label"l10",
    label"l11",
    label"l12",
    label"l13",
    label"l14",
    label"l15",
    label"l16",
    label"l17",
    label"l18",
    label"l19",
    label"l20",
    label"l21",
    label"l22"
  )
```

We will obviously get a compilation error if we try to provide an incorrect
number of values when using this counter:

```tut:fail
lotsOfLabels.labelValues("1val", "2val").inc()
```

## The Registry

The `Registry` is a service to which collectors are registered. You can
then call `collect` to obtain current monitoring values for all the
registered collectors.

You can create a registry with a default implementation with:

```tut
implicit val defaultRegistry = DefaultRegistry()
```

```tut
implicit val histogramBuckets = HistogramBuckets(1, 2, 5, 10, 20, 50, 100)

val activeRequests = Gauge(metric"active_requests", "Active requests").labels().unsafeRegister
val numErrors = Counter(metric"num_errors", "Total errors").labels().unsafeRegister
val requestLatency = Histogram(metric"request_latency", "Request latency").labels(label"path").unsafeRegister

activeRequests.set(50)
numErrors.inc
requestLatency.labelValues("/home").observe(17)
implicitly[Registry]
```

## Using with FS2 Task (WIP)

Both gauges and histograms can be used to time FS2 Tasks (or any type which has an `fs2.util.Effect` instance).

Certain imports are needed:

```tut:reset
import fs2._
import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.fs2_syntax._
```

Then the method `timeSuccess` can be used to capture the duration of the task (in seconds):

```tut:silent
implicit val registry = DefaultRegistry()
```
```tut
implicit val histogramBuckets = HistogramBuckets(0.02, 0.05, 0.1, 0.2, 0.5, 1.0)
val requestLatency = Histogram(metric"request_latency", "Request latency").labels(label"path").unsafeRegister

val mySleepyTask = Task.delay(Thread.sleep(scala.util.Random.nextInt(800)))
val myTimedSleepyTask = mySleepyTask.timeSuccess(requestLatency.labelValues("/home"))

for (i <- Range(1, 10)) myTimedSleepyTask.unsafeRun

implicitly[Registry]
```

## Exposing JMX Statistics

Some JVM statistics can be exposed with:

```tut:silent
implicit val registry = DefaultRegistry()
```
```tut
import fs2._
import org.lyranthe.prometheus.client._

jmx.unsafeRegister

println(implicitly[Registry])
```
