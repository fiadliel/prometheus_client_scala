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

```tut
import org.lyranthe.prometheus.client._
```

## String interpolators

Metric names and labels have certain requirements for what characters are allowed. To
allow this to be correctly checked at compile time, two string interpolators are provided:

 - `metric""` creates metric names
 - `label""` creates label names

These interpolators require constant values, some examples are shown below.

Some valid metric names:

```tut
metric"http_requests_total"

final val system = "http"
final val subsystem = "requests"
metric"${system}_${subsystem}_total"
```

Some invalid metric names:

```tut:fail
metric"1"
```

```tut
val subsystem = "requests"
```
```tut:fail
metric"${system}_${subsystem}_total"
```

### Creating a monitoring variable

To create a simple counter:

```tut
val totalRequests = Counter(metric"total_requests", "Total requests").labels()
```

The resulting counter has a metric name `total_requests`, a help message with the contents,
"Total requests", and has no labels.

You can use this in this way:

```tut
totalRequests.inc()
```

### Creating a monitoring variable with labels

If you need labels attached to the counter, specify the label names using
the `.labels` method:

```tut
val totalErrors = Counter(metric"total_errors", "Total errors").labels(label"code")
```

To increment a counter with an error code of "404", one might
do the following:

```tut
totalErrors.labelValues("404").inc()
```

### Behavior with incorrect input

We will obviously get a compilation error if we try to provide an incorrect
number of values when using this counter:

```tut:fail
totalErrors.labelValues("404", "/path").inc()
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

val activeRequests = Gauge(metric"active_requests", "Active requests").labels().register
val numErrors = Counter(metric"total_errors", "Total errors").labels().register
val requestLatency = Histogram(metric"db_request_duration_seconds", "Request latency\nfrom database").labels(label"sql").register

activeRequests.set(50)
numErrors.inc
requestLatency.labelValues("select \nname, age\nfrom\nusers").observe(17)
implicitly[Registry].outputText
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
implicit val defaultRegistry = DefaultRegistry()
```
```tut
implicit val histogramBuckets = HistogramBuckets(0.02, 0.05, 0.1, 0.2, 0.5, 1.0)
val requestLatency = Histogram(metric"request_latency", "Request latency").labels(label"path").register

val mySleepyTask = Task.delay(Thread.sleep(scala.util.Random.nextInt(800)))
val myTimedSleepyTask = mySleepyTask.timeSuccess(_ => requestLatency.labelValues("/home"))

for (i <- Range(1, 10)) myTimedSleepyTask.unsafeRun

implicitly[Registry].outputText
```

## Using with Play 2.4.x

It is possible to add instrumentation to your 2.4.x Play app Controllers very easily.
A [Play filter](https://www.playframework.com/documentation/2.4.x/ScalaHttpFilters) named `PrometheusFilter` is available in the `play` contrib module and will add a Histogram metric named `http_request_duration_seconds` with the following labels:
* method (the http method, i.e. GET / PUT)
* path (the route path)
* status (the http response code family, i.e. 2xx, 4xx, 5xx)

Create a `Filter` class in the root of your `Play` service with the following implementation:

```scala
import com.google.inject.Inject
import org.lyranthe.prometheus.client.integration.play.filters.PrometheusFilter
import play.api.http.HttpFilters

class Filters @Inject()(prometheusFilter: PrometheusFilter) extends HttpFilters {

  val filters = Seq(prometheusFilter)

}
```

Please note that an implementation of a `org.lyranthe.prometheus.client.Registry` must be availabe in the *Guice* context.

An example:

```scala
import com.google.inject.AbstractModule

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[Registry]).to(classOf[DefaultRegistry])
  }
  
}
```

## Exposing JMX Statistics

Some JVM statistics can be exposed with:

```tut:silent
implicit val defaultRegistry = DefaultRegistry()
```
```tut
import fs2._
import org.lyranthe.prometheus.client._

jmx.register

println(implicitly[Registry].outputText)
```

