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

scala> implicit val totalRequests = Counter.create("total_requests")
totalRequests: io.prometheus.client.scala.internal.counter.Counter0[String("total_requests")] = io.prometheus.client.scala.internal.counter.Counter0@47c308c7
```

Note that the counter is a `Counter0`, which means that it
has no labels. Therefore, it needs no corresponding label values
when incrementing the counter.

### Finding the collector

You can find this counter in the implicit scope like this:

```scala
scala> Counter.lookup("total_requests").inc
```

If the variable is not currently available, this would be a
compilation error:

```scala
scala> Counter.lookup("no_such_variable").inc
<console>:17: error: could not find implicit value for parameter e: io.prometheus.client.scala.internal.counter.Counter0[String("no_such_variable")]
       Counter.lookup("no_such_variable").inc
                     ^
```

You can always use the counter variable directly, though
there is no performance benefit in this (it could make your
code clearer in some cases):

```scala
scala> totalRequests.inc
```

### Creating collectors with labels

Any extra strings passed when creating a collector, represent
labels for any monitoring variables. Whenever any information is
passed to the monitoring system, an appropriate number of label
values need to be provided; one for each label.

```scala
scala> implicit val totalErrors = Counter.create("total_errors", "code")
totalErrors: io.prometheus.client.scala.internal.counter.Counter1[String("total_errors"),String("code")] = io.prometheus.client.scala.internal.counter.Counter1@5a5dd769
```

### Using collectors with labels

The types of the collectors include the label names, so we can
look these up in implicit scope again.

To increment a counter with an error code of "404", one might
do the following:

```scala
scala> Counter.lookup("total_errors", "code").inc("404")
```
