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

scala> implicit val totalRequests = Counter.create("total_requests")()
totalRequests: io.prometheus.client.scala.internal.counter.Counter0[String("total_requests")] = Counter0(total_requests)()
```

Note that the counter is a `Counter0`, which means that it
has no labels. Therefore, it needs no corresponding label values
when incrementing the counter.

### Finding the collector

You can find this counter in the implicit scope like this:

```scala
scala> Counter.lookup("total_requests")().inc
```

If the variable is not currently available, this would be a
compilation error:

```scala
scala> Counter.lookup("no_such_variable")().inc
<console>:17: error: could not find implicit value for parameter e: io.prometheus.client.scala.internal.counter.Counter0[String("no_such_variable")]
       Counter.lookup("no_such_variable")().inc
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
scala> implicit val totalErrors = Counter.create("total_errors")("code")
totalErrors: io.prometheus.client.scala.internal.counter.Counter1[String("total_errors"),String("code")] = Counter1(total_errors)(code)
```

### Using collectors with labels

The types of the collectors include the label names, so we can
look these up in implicit scope again.

To increment a counter with an error code of "404", one might
do the following:

```scala
scala> Counter.lookup("total_errors")("code").inc("404")
```

This is supported up to 22 labels, for example:

```scala
scala> implicit val lotsOfLabels =
     |   Counter.create("lots_of_labels")(
     |     "1",
     |     "2",
     |     "3",
     |     "4",
     |     "5",
     |     "6",
     |     "7",
     |     "8",
     |     "9",
     |     "10",
     |     "11",
     |     "12",
     |     "13",
     |     "14",
     |     "15",
     |     "16",
     |     "17",
     |     "18",
     |     "19",
     |     "20",
     |     "21",
     |     "22"
     |   )
lotsOfLabels: io.prometheus.client.scala.internal.counter.Counter22[String("lots_of_labels"),String("1"),String("2"),String("3"),String("4"),String("5"),String("6"),String("7"),String("8"),String("9"),String("10"),String("11"),String("12"),String("13"),String("14"),String("15"),String("16"),String("17"),String("18"),String("19"),String("20"),String("21"),String("22")] = Counter21(lots_of_labels)(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22)
```

We will obviously get a compilation error if we try to provide an incorrect
number of values when using this collector:

```scala
scala> Counter.lookup("lots_of_labels")(
     |     "1",
     |     "2",
     |     "3",
     |     "4",
     |     "5",
     |     "6",
     |     "7",
     |     "8",
     |     "9",
     |     "10",
     |     "11",
     |     "12",
     |     "13",
     |     "14",
     |     "15",
     |     "16",
     |     "17",
     |     "18",
     |     "19",
     |     "20",
     |     "21",
     |     "22"
     |   ).inc("1val", "2val")
<console>:42: error: not enough arguments for method inc: (l1: String, l2: String, l3: String, l4: String, l5: String, l6: String, l7: String, l8: String, l9: String, l10: String, l11: String, l12: String, l13: String, l14: String, l15: String, l16: String, l17: String, l18: String, l19: String, l20: String, l21: String, l22: String)Unit.
Unspecified value parameters l3, l4, l5...
         ).inc("1val", "2val")
              ^
```
  
