import scala.sys.process._

enablePlugins(ScalaJSPlugin)

organization in Global := "org.lyranthe.prometheus"

val scala211 = "2.11.12"
val scala212 = "2.12.8"

version in ThisBuild := "git describe --tags --dirty --always".!!.stripPrefix("v").trim
scalacOptions in (Compile, doc) in ThisBuild ++= Seq("-groups",
                                                     "-implicits",
                                                     "-implicits-show-all",
                                                     "-diagrams")
sonatypeProfileName := "org.lyranthe"
publishArtifact in ThisBuild := false
scalaVersion in ThisBuild := scala211
crossScalaVersions in ThisBuild := Seq(scala211, scala212)

// Add sonatype repository settings
publishTo in ThisBuild := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

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
    <developers>
      <developer>
        <id>fiadliel</id>
        <name>Gary Coady</name>
        <url>https://www.lyranthe.org/</url>
      </developer>
    </developers>
  }
)

val macros =
  crossProject
    .in(file("macros"))
    .settings(publishSettings)
    .settings(
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212)
    )

val macrosJVM = macros.jvm
val macrosJS  = macros.js

val client =
  crossProject
    .in(file("client"))
    .enablePlugins(spray.boilerplate.BoilerplatePlugin)
    .settings(publishSettings)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212),
      boilerplateSource in Compile := baseDirectory.value.getParentFile / "shared" / "src" / "main" / "boilerplate",
      apiURL := Some(url(
        raw"https://oss.sonatype.org/service/local/repositories/public/archive/org/lyranthe/prometheus/client_${scalaBinaryVersion.value}/${version.value}/client_${scalaBinaryVersion.value}-${version.value}-javadoc.jar/!/index.html"))
    )
    .dependsOn(macros)

val clientJVM = client.jvm
val clientJS  = client.js

val protobuf =
  project
    .in(file("protobuf"))
    .settings(publishSettings)
    .settings(
      PB.targets in Compile := Seq(
        scalapb.gen() -> (sourceManaged in Compile).value
      ),
      scalaVersion := scala211,
      PB.protocVersion := "-v330",
      crossScalaVersions := Seq(scala211, scala212)
    )
    .dependsOn(clientJVM)

val cats =
  crossProject
    .in(file("cats"))
    .settings(publishSettings)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212),
      libraryDependencies += "org.typelevel" %%% "cats-effect" % "1.2.0"
    )
    .dependsOn(client)

val catsJVM = cats.jvm
val catsJS  = cats.js

val benchmark =
  project
    .in(file("benchmark"))
    .enablePlugins(JmhPlugin)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211),
      libraryDependencies += "io.prometheus" % "simpleclient" % "0.6.0"
    )
    .dependsOn(clientJVM)

val play24 =
  project
    .in(file("play24"))
    .settings(publishSettings)
    .settings(
      yax(file("yax/play"), "play245"),
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211),
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play" % "2.4.11" % "provided" withSources ()
      )
    )
    .dependsOn(clientJVM)

val play25 =
  project
    .in(file("play25"))
    .settings(publishSettings)
    .settings(
      yax(file("yax/play"), "play245", "play256"),
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211),
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play" % "2.5.18" % "provided" withSources ()
      )
    )
    .dependsOn(clientJVM)

val play26 =
  project
    .in(file("play26"))
    .settings(publishSettings)
    .settings(
      yax(file("yax/play"), "play256", "play26"),
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212),
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play"       % "2.6.11" % "provided" withSources (),
        "com.typesafe.play" %% "play-guice" % "2.6.11" % "provided" withSources ()
      )
    )
    .dependsOn(clientJVM)

val akkaHttp =
  project
    .in(file("akka-http"))
    .settings(publishSettings)
    .settings(
      name := "akka-http",
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212),
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http" % "10.1.7" % "provided" withSources (),
        "com.typesafe.akka" %% "akka-stream" % "2.5.20" % "provided" withSources ()
      )
    )
    .dependsOn(clientJVM)

import com.typesafe.sbt.SbtGit.GitKeys._

val site =
  project
    .in(file("site"))
    .enablePlugins(HugoPlugin, GhpagesPlugin, ScalaUnidocPlugin, TutPlugin)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211),
      baseURL in Hugo := new URI(
        "https://www.lyranthe.org/prometheus_client_scala"),
      includeFilter in Hugo := "*.css" | "*.js" | "*.png" | "*.jpg" | "*.txt" | "*.html" | "*.md" | "*.rst" | "*.woff" | "*.ttf",
      siteSubdirName in SiteScaladoc := "latest/api",
      ghpagesNoJekyll := false,
      addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc),
                           siteSubdirName in SiteScaladoc),
      siteMappings ++= tut.value,
      unidocProjectFilter in (ScalaUnidoc, unidoc) :=
        inProjects(clientJVM, macrosJVM, play26, protobuf, catsJVM),
      gitRemoteRepo := "git@github.com:fiadliel/prometheus_client_scala.git"
    )
    .dependsOn(clientJVM, macrosJVM, play26, protobuf, catsJVM)
