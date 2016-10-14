val commonSettings = Seq(
  organization := "org.lyranthe.prometheus",
  scalaVersion := "2.11.8",
  version := "git describe --tags --dirty --always".!!.stripPrefix("v").trim,
  crossScalaVersions := Seq("2.11.8", "2.12.0-RC1")
)

val publishSettings = Seq(
  sonatypeProfileName := "org.lyranthe",
  pomExtra in Global := {
    <url>https://github.com/fiadliel/prometheus_client_scala</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>https://github.com/fiadliel/prometheus_client_scala/blob/master/LICENSE</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/fiadliel/prometheus_scala_client.git</connection>
      <developerConnection>scm:git:git@github.com:fiadliel/prometheus_scala_client.git</developerConnection>
      <url>github.com/fiadliel/prometheus_scala_client</url>
    </scm>
    <developers>
      <developer>
        <id>fiadliel</id>
        <name>Gary Coady</name>
        <url>https://www.lyranthe.org/</url>
      </developer>
    </developers>
  }
)

scalafmtConfig in ThisBuild := Some(file(".scalafmt.conf"))

val client =
  project.in(file("client")).enablePlugins(spray.boilerplate.BoilerplatePlugin).settings(commonSettings)

val jmx =
  project.in(file("jmx")).settings(commonSettings).dependsOn(client)

val fs2 =
  project
    .in(file("fs2"))
    .settings(commonSettings)
    .settings(
      libraryDependencies += "co.fs2" %% "fs2-core" % "0.9.1"
    )
    .dependsOn(client)

val doc =
  project
    .in(file("doc"))
    .settings(commonSettings)
    .settings(tutSettings)
    .settings(
      tutSourceDirectory := baseDirectory.value / "src",
      tutTargetDirectory := baseDirectory.value
    )
    .dependsOn(client, fs2, jmx)

val benchmark =
  project
    .in(file("benchmark"))
    .settings(commonSettings)
    .enablePlugins(JmhPlugin)
    .settings(
      libraryDependencies += "io.prometheus" % "simpleclient" % "0.0.16"
    )
    .dependsOn(client)
