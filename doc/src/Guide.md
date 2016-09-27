# Introduction to Prometheus Scala client

## Creating and finding monitoring variables

This scala client makes extensive use of the scala type system when
creating monitoring collectors. Every collector has a distinctive
type, based on the collector name, and any labels attached.

You can therefore use the implicit scope to store and lookup
monitoring collectors.

### Creating a collector

Here is an example where a simple counter is created:

```tut
import io.prometheus.client.scala._

val totalRequests = Counter("total_requests", "Total requests").labels()
```

Note that the counter is a `Counter0`, which means that it
has no labels. Therefore, it needs no corresponding label values
when incrementing the counter.

You can use this counter:

```tut
totalRequests.inc()
```

### Creating collectors with labels

Any extra strings passed when creating a collector, represent
labels for any monitoring variables. Whenever any information is
passed to the monitoring system, an appropriate number of label
values need to be provided; one for each label.

```tut
val totalErrors = Counter("total_errors", "Total errors").labels("code")
```

### Using collectors with labels

To increment a counter with an error code of "404", one might
do the following:

```tut
totalErrors.labelValues("404").inc()
```

This is supported up to 22 labels, for example:

```tut
val lotsOfLabels =
  Counter("lots_of_labels", "Lots of labels").labels(
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
    "10",
    "11",
    "12",
    "13",
    "14",
    "15",
    "16",
    "17",
    "18",
    "19",
    "20",
    "21",
    "22"
  )
```

We will obviously get a compilation error if we try to provide an incorrect
number of values when using this collector:

```tut:fail
lotsOfLabels.labelValues("1val", "2val").inc()
```

## The Registry

The `Registry` is a service to which collectors are registered. You can
then call `collect` to obtain current monitoring values for all the
registered collectors.

There is a default registry available, which is used if no other registry
is specified.

```tut
implicit val histogramBuckets = HistogramBuckets(1, 2, 5, 10, 20, 50, 100)

val activeRequests = Gauge("active_requests", "Active requests").labels().register
val numErrors = Counter("num_errors", "help").labels().register
val requestLatency = Histogram("request_latency", "path").register

activeRequests.set(50)
numErrors.inc
requestLatency.observe("/home")(17)
implicitly[Registry].collect
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
