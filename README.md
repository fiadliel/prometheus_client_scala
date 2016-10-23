# Prometheus client for Scala

This Prometheus client offers an idiomatic API for instrumenting applications written in Scala. It tries to provide an API which is efficient, easy to use. Also, as far as possible, it tries to report API usage errors at compile time instead of runtime.

In addition, it offers some extra insight into your program by exposing some useful VM statistics.

## Adding the library to your build

```scala
libraryDependencies += "org.lyranthe.prometheus" %% "client" % "0.4.0"
```

## Documentation

See the [guide](doc/Guide.md) for examples of usage.

 - [Client API](https://oss.sonatype.org/service/local/repositories/releases/archive/org/lyranthe/prometheus/client_2.12.0-RC2/0.4.0/client_2.12.0-RC2-0.4.0-javadoc.jar/!/index.html#org.lyranthe.prometheus.client.package)
 - [FS2 Syntax Extension API](https://oss.sonatype.org/service/local/repositories/releases/archive/org/lyranthe/prometheus/fs2_2.12.0-RC2/0.4.0/fs2_2.12.0-RC2-0.4.0-javadoc.jar/!/index.html#org.lyranthe.prometheus.client.fs2_syntax$$EffectExtraSyntax)
