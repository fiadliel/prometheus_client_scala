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

implicit val totalRequests = Counter.create("total_requests")()
```

Note that the counter is a `Counter0`, which means that it
has no labels. Therefore, it needs no corresponding label values
when incrementing the counter.

### Finding the collector

You can find this counter in the implicit scope like this:

```tut
Counter.lookup("total_requests")().inc
```

If the variable is not currently available, this would be a
compilation error:

```tut:fail
Counter.lookup("no_such_variable")().inc
```

You can always use the counter variable directly, though
there is no performance benefit in this (it could make your
code clearer in some cases):

```tut
totalRequests.inc
```

### Creating collectors with labels

Any extra strings passed when creating a collector, represent
labels for any monitoring variables. Whenever any information is
passed to the monitoring system, an appropriate number of label
values need to be provided; one for each label.

```tut
implicit val totalErrors = Counter.create("total_errors")("code")
```

### Using collectors with labels

The types of the collectors include the label names, so we can
look these up in implicit scope again.

To increment a counter with an error code of "404", one might
do the following:

```tut
Counter.lookup("total_errors")("code").inc("404")
```

This is supported up to 22 labels, for example:

```tut
implicit val lotsOfLabels =
  Counter.create("lots_of_labels")(
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
Counter.lookup("lots_of_labels")(
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
  ).inc("1val", "2val")
```

## The Registry

The `Registry` is a service to which collectors are registered. You can
then call `collect` to obtain current monitoring values for all the
registered collectors.

There is a default registry available, which is used if no other registry
is specified.

```tut
implicit val activeRequests = Gauge.create("active_requests")().register
implicit val numErrors = Counter.create("num_errors")().register
implicit val requestLatency = Histogram.create("request_latency", Seq(1, 2, 5, 10, 20, 50, 100))("path").register
Gauge.lookup("active_requests")().set(50)
Counter.lookup("num_errors")().inc
Histogram.lookup("request_latency")("path").observe("/home")(17)
implicitly[Registry].collect
```

## Using with FS2 Task

Both gauges and histograms can be used to time FS2 Tasks (or any type which implements `fs2.util.Suspendable`).

Certain imports are needed:

```tut
import io.prometheus.client.scala.fs2_syntax._
import fs2._
```

Then the method `timeEffect` can be used to capture the duration of the task (in seconds):

```tut
implicit val requestLatency = Histogram.create("request_latency", Seq(0.02, 0.05, 0.1, 0.2, 0.5, 1.0))()
val mySleepyTask = Task.delay(Thread.sleep(scala.util.Random.nextInt(1200)))
val myTimedSleepyTask = Histogram.lookup("request_latency")().timeEffect(mySleepyTask)

for (i <- Range(1, 10)) myTimedSleepyTask.unsafeRun

Histogram.lookup("request_latency")().collect
```
