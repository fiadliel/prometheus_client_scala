import sbtprotobuf.{ProtobufPlugin => PB}

enablePlugins(ScalaJSPlugin)

organization in Global := "org.lyranthe.prometheus"

val scala211 = "2.11.8"
val scala212 = "2.12.1"

version in ThisBuild := "git describe --tags --dirty --always".!!.stripPrefix(
  "v").trim
scalacOptions in (Compile, doc) in ThisBuild ++= Seq("-groups",
                                                     "-implicits",
                                                     "-implicits-show-all",
                                                     "-diagrams")
sonatypeProfileName := "org.lyranthe"
publishArtifact in ThisBuild := false
enablePlugins(CrossPerProjectPlugin)
scalaVersion in ThisBuild := scala211
crossScalaVersions in ThisBuild := Seq(scala211, scala212)

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
      <connection>scm:git:github.com/fiadliel/prometheus_client_scala.git</connection>
      <developerConnection>scm:git:git@github.com:fiadliel/prometheus_client_scala.git</developerConnection>
      <url>github.com/fiadliel/prometheus_client_scala</url>
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
    .settings(PB.protobufSettings)
    .settings(
      scalaVersion := scala211,
      version in protobufConfig := "3.3.0",
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
      libraryDependencies += "org.typelevel" %%% "cats-effect" % "0.3"
    )
    .dependsOn(client)

val catsJVM = cats.jvm
val catsJS  = cats.js

val fs2 =
  crossProject
    .in(file("fs2"))
    .settings(publishSettings)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212),
      libraryDependencies += "co.fs2" %%% "fs2-core" % "0.9.2"
    )
    .dependsOn(client)

val fs2JVM = fs2.jvm
val fs2JS  = fs2.js

val scalaz72 =
  project
    .in(file("scalaz"))
    .settings(publishSettings)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212),
      libraryDependencies += "org.scalaz" %% "scalaz-concurrent" % "7.2.8"
    )
    .dependsOn(clientJVM)

val monix21 =
  crossProject
    .in(file("monix"))
    .settings(publishSettings)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212),
      libraryDependencies += "io.monix" %%% "monix-eval" % "2.1.2"
    )
    .dependsOn(client)

val monix21JVM = monix21.jvm
val monix21JS  = monix21.js

val benchmark =
  project
    .in(file("benchmark"))
    .enablePlugins(JmhPlugin)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211),
      libraryDependencies += "io.prometheus" % "simpleclient" % "0.0.20"
    )
    .dependsOn(clientJVM)

val play24 =
  project
    .in(file("play24"))
    .settings(publishSettings)
    .settings(
      yax(file("yax/play"), "play24"),
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211),
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play" % "2.4.10" % "provided" withSources ()
      )
    )
    .dependsOn(clientJVM)

val play25 =
  project
    .in(file("play25"))
    .settings(publishSettings)
    .settings(
      yax(file("yax/play"), "play25"),
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211),
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play" % "2.5.12" % "provided" withSources ()
      )
    )
    .dependsOn(clientJVM)

val akkaHttp =
  crossProject
    .in(file("akka-http"))
    .settings(publishSettings)
    .settings(
      name := "akka-http",
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212),
      libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.9"
    )
    .dependsOn(client)

val akkaHttpJVM = akkaHttp.jvm
val akkaHttpJS  = akkaHttp.js

// Site Settings
import com.typesafe.sbt.site._
import com.typesafe.sbt.site.SitePlugin.autoImport.siteSubdirName
import com.typesafe.sbt.site.util.SiteHelpers
import com.typesafe.sbt.SbtGhPages.GhPagesKeys.ghpagesNoJekyll
import com.typesafe.sbt.SbtGit.GitKeys._

val site =
  project
    .in(file("site"))
    .enablePlugins(HugoPlugin)
    .settings(unidocSettings)
    .settings(tutSettings)
    .settings(ghpages.settings)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211),
      baseURL in Hugo := new URI(
        "https://www.lyranthe.org/prometheus_client_scala"),
      includeFilter in Hugo := "*.css" | "*.js" | "*.png" | "*.jpg" | "*.txt" | "*.html" | "*.md" | "*.rst" | "*.woff" | "*.ttf",
      siteSubdirName in SiteScaladoc := "latest/api",
      ghpagesNoJekyll := false,
      SiteHelpers.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc),
                                       siteSubdirName in SiteScaladoc),
      siteMappings ++= tut.value,
      UnidocKeys.unidocProjectFilter in (ScalaUnidoc, UnidocKeys.unidoc) :=
        inProjects(clientJVM,
                   macrosJVM,
                   play25,
                   protobuf,
                   fs2JVM,
                   monix21JVM,
                   scalaz72),
      gitRemoteRepo := "git@github.com:fiadliel/prometheus_client_scala.git"
    )
    .dependsOn(clientJVM, fs2JVM)
