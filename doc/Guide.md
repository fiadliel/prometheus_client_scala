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
totalRequests: io.prometheus.client.scala.internal.counter.Counter0[String("total_requests")] = io.prometheus.client.scala.internal.counter.Counter0@338a1d99
```


