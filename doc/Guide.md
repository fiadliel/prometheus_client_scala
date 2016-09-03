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
totalRequests: io.prometheus.client.scala.internal.counter.Counter0[String("total_requests")] = io.prometheus.client.scala.internal.counter.Counter0@48ed195f
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
