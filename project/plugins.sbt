addSbtPlugin("io.spray" % "sbt-boilerplate" % "0.6.1")

addSbtPlugin("com.lucidchart" % "sbt-scalafmt"          % "1.16")
addSbtPlugin("com.lucidchart" % "sbt-scalafmt-coursier" % "1.16")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.0")

resolvers += "Artifactory Realm" at "https://iadvize.jfrog.io/iadvize/iadvize-sbt"

credentials += Credentials(
  "Artifactory Realm",
  "iadvize.jfrog.io",
  (sys.env.get("ARTIFACTORY_USERNAME") orElse sys.props.get("ARTIFACTORY_USERNAME")).getOrElse(""),
  (sys.env.get("ARTIFACTORY_PASS") orElse sys.props.get("ARTIFACTORY_PASS")).getOrElse("")
)
addSbtPlugin("com.iadvize" % "sbt-iadvize-plugin" % "4.1.0")
