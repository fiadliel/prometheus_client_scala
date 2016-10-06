val commonSettings = Seq(
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.0-RC1")
)

val core =
  project
    .in(file("core"))
    .enablePlugins(spray.boilerplate.BoilerplatePlugin)
    .settings(commonSettings)
    .settings(
      name := "core"
    )

val fs2 =
  project
    .in(file("fs2"))
    .settings(commonSettings)
    .settings(
      libraryDependencies += "co.fs2" %% "fs2-core" % "0.9.1"
    )
    .dependsOn(core)

val doc =
  project
    .in(file("doc"))
    .settings(commonSettings)
    .settings(tutSettings)
    .settings(
      tutSourceDirectory := baseDirectory.value / "src",
      tutTargetDirectory := baseDirectory.value
    )
    .dependsOn(core, fs2)

val benchmark =
  project
    .in(file("benchmark"))
    .settings(commonSettings)
    .enablePlugins(JmhPlugin)
    .settings(
      libraryDependencies += "io.prometheus" % "simpleclient" % "0.0.16"
    )
    .dependsOn(core)
