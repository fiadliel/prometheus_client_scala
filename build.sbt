import sbtprotobuf.{ProtobufPlugin => PB}

organization in Global := "org.lyranthe.prometheus"

val scala211 = "2.11.8"
val scala212 = "2.12.0"

version in ThisBuild := "git describe --tags --dirty --always".!!
  .stripPrefix("v")
  .trim
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

scalafmtConfig in ThisBuild := Some(file(".scalafmt.conf"))

val macros =
  project
    .in(file("macros"))
    .settings(publishSettings)
    .settings(
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212)
    )

val client =
  project
    .in(file("client"))
    .enablePlugins(spray.boilerplate.BoilerplatePlugin)
    .settings(publishSettings)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212),
      apiURL := Some(url(
        raw"https://oss.sonatype.org/service/local/repositories/public/archive/org/lyranthe/prometheus/client_${scalaBinaryVersion.value}/${version.value}/client_${scalaBinaryVersion.value}-${version.value}-javadoc.jar/!/index.html"))
    )
    .dependsOn(macros)

val protobuf =
  project
    .in(file("protobuf"))
    .settings(publishSettings)
    .settings(PB.protobufSettings)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212)
    )
    .dependsOn(client)

val fs2 =
  project
    .in(file("fs2"))
    .settings(publishSettings)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211, scala212),
      libraryDependencies += "co.fs2" %% "fs2-core" % "0.9.2"
    )
    .dependsOn(client)

val benchmark =
  project
    .in(file("benchmark"))
    .enablePlugins(JmhPlugin)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211),
      libraryDependencies += "io.prometheus" % "simpleclient" % "0.0.18"
    )
    .dependsOn(client)

val play24 =
  project
    .in(file("play24"))
    .settings(publishSettings)
    .settings(
      scalaVersion := scala211,
      crossScalaVersions := Seq(scala211),
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play" % "2.4.8" % "provided" withSources ()
      )
    )
    .dependsOn(client)

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
      gitRemoteRepo := "git@github.com:fiadliel/prometheus_client_scala.git"
    )
    .dependsOn(client, fs2)
