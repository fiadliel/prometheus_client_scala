val commonSettings = Seq(
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.0-M5")
)

val core = project.in(file("core")).settings(commonSettings).enablePlugins(spray.boilerplate.BoilerplatePlugin).settings(
  name := "fs2-client",
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )
)

val benchmark = project.settings(commonSettings).enablePlugins(JmhPlugin).dependsOn(core).settings(
  libraryDependencies += "io.prometheus" % "simpleclient" % "0.0.16"
)

ghpages.settings
site.includeScaladoc()
git.remoteRepo := "git@github.com:fiadliel/prometheus_client_scala.git"
