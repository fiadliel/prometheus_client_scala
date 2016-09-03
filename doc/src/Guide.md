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

implicit val totalRequests = Counter.create("total_requests")
```


