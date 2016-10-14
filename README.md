# prometheus_client_scala

Work in progress for a Scala client.

## Adding the library to your build

```scala
libraryDependencies += "org.lyranthe.prometheus" %% "client" % "0.0.1"
```

## Still to do

 - export of collector data over HTTP
 - still experimenting with FS2 wrappers (e.g. how to represent one counter for successes, another for failure)
 - sanity checking names against allowed regular expressions

See the [guide](doc/Guide.md) for examples of the current state of the library.
