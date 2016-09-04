val commonSettings = Seq(
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.0-M5")
)

val base =
  project.in(file("base"))
    .settings(commonSettings)
    .enablePlugins(spray.boilerplate.BoilerplatePlugin)
    .settings(
      name := "base",
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )

val core =
  project.in(file("core"))
    .settings(commonSettings)
    .settings(
      name := "core",
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
    ).dependsOn(base)

val benchmark =
  project.in(file("benchmark"))
    .settings(commonSettings)
    .enablePlugins(JmhPlugin)
    .settings(
      libraryDependencies += "io.prometheus" % "simpleclient" % "0.0.16"
    )
    .dependsOn(core)

val fs2 =
  project.in(file("fs2"))
    .settings(commonSettings)
    .enablePlugins(spray.boilerplate.BoilerplatePlugin)
    .settings(
      libraryDependencies += "co.fs2" %% "fs2-core" % "0.9.0-RC2"
    )
    .dependsOn(core)

val doc =
  project.in(file("doc"))
    .settings(commonSettings)
    .settings(tutSettings)
    .settings(
      tutSourceDirectory := baseDirectory.value / "src",
      tutTargetDirectory := baseDirectory.value
    ).dependsOn(core)
