val lastScalaVersion = "2.13.2"

organization in Global := "org.lyranthe.prometheus"
version in ThisBuild := "0.9.0-M5"
scalaVersion in ThisBuild := lastScalaVersion

lazy val macros =
  project
    .enablePlugins(ArtifactoryPlugin)
    .in(file("macros"))
    .settings(
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      publishTo := Some("Artifactory Realm" at "https://iadvize.jfrog.io/iadvize/iadvize-sbt"),
    )

lazy val client =
  project
    .in(file("client"))
    .enablePlugins(spray.boilerplate.BoilerplatePlugin, ArtifactoryPlugin)
    .settings(
      publishTo := Some("Artifactory Realm" at "https://iadvize.jfrog.io/iadvize/iadvize-sbt"),
    )
    .dependsOn(macros)

lazy val play28 =
  project
    .enablePlugins(ArtifactoryPlugin, GlobalSettingsPlugin, FormattingPlugin)
    .in(file("play28"))
    .settings(
      name := "play28",
      publishTo := Some("Artifactory Realm" at "https://iadvize.jfrog.io/iadvize/iadvize-sbt"),
      libraryDependencies ++= Seq(
        ("com.typesafe.play" %% "play"       % "2.8.2" % Provided).withSources(),
        ("com.typesafe.play" %% "play-guice" % "2.8.2" % Provided).withSources()
      )
    )
    .dependsOn(client)
