import sbtprotobuf.{ProtobufPlugin=>PB}

organization in Global := "org.lyranthe.prometheus"

scalaVersion in ThisBuild := "2.11.8"
crossScalaVersions in ThisBuild := Seq("2.11.8", "2.12.0-RC1")

version in ThisBuild := "git describe --tags --dirty --always".!!.stripPrefix("v").trim
scalacOptions in (Compile, doc) in ThisBuild ++= Seq("-groups", "-implicits", "-implicits-show-all", "-diagrams")
sonatypeProfileName := "org.lyranthe"
publishArtifact in ThisBuild := false

enablePlugins(MicrositesPlugin)

val publishSettings = Seq(
  mimaPreviousArtifacts := Set(organization.value %% name.value % "0.2.0"),
  publishArtifact := true,
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
micrositeName := "Prometheus Scala Client"
micrositeDescription := "Scala client for Prometheus monitoring system"
micrositeAuthor := "Gary Coady <gary@lyranthe.org>"
micrositeGithubOwner := "fiadliel"
micrositeGithubRepo := "prometheus_client_scala"

lazy val root = project.in(file(".")).dependsOn(client, fs2)

val macros =
  project
    .in(file("macros"))
    .settings(publishSettings)
    .settings(
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )

val client =
  project
    .in(file("client"))
    .enablePlugins(spray.boilerplate.BoilerplatePlugin)
    .settings(publishSettings)
    .settings(
      apiURL := Some(url(
        raw"https://oss.sonatype.org/service/local/repositories/public/archive/org/lyranthe/prometheus/client_${scalaBinaryVersion.value}/${version.value}/client_${scalaBinaryVersion.value}-${version.value}-javadoc.jar/!/index.html"))
    )
    .dependsOn(macros)

val protobuf =
  project
    .in(file("protobuf"))
    .settings(publishSettings)
    .settings(PB.protobufSettings)
    .dependsOn(client)

val fs2 =
  project
    .in(file("fs2"))
    .settings(publishSettings)
    .settings(
      libraryDependencies += "co.fs2" %% "fs2-core" % "0.9.1",
      apiURL := Some(url(
        raw"https://oss.sonatype.org/service/local/repositories/public/archive/org/lyranthe/prometheus/fs2_${scalaBinaryVersion.value}/${version.value}/fs2_${scalaBinaryVersion.value}-${version.value}-javadoc.jar/!/index.html"))
    )
    .dependsOn(client)

val benchmark =
  project
    .in(file("benchmark"))
    .enablePlugins(JmhPlugin)
    .settings(
      libraryDependencies += "io.prometheus" % "simpleclient" % "0.0.16"
    )
    .dependsOn(client)
