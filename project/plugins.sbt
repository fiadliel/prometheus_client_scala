addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.20")

addSbtPlugin("pl.project13.scala" % "sbt-jmh"         % "0.2.27")
addSbtPlugin("io.spray"           % "sbt-boilerplate" % "0.6.1")
addSbtPlugin("org.tpolecat"       % "tut-plugin"      % "0.6.1")
addSbtPlugin("com.geirsson"       % "sbt-scalafmt"    % "1.2.0")

addSbtPlugin("com.thoughtworks.sbt-api-mappings" % "sbt-api-mappings" % "2.0.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")
addSbtPlugin("com.jsuereth"   % "sbt-pgp"      % "1.1.0")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.18")

addSbtPlugin(
  "com.thesamet" % "sbt-protoc" % "0.99.12" exclude ("com.trueaccord.scalapb", "protoc-bridge_2.12"))

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin-shaded" % "0.6.6"

addSbtPlugin("com.typesafe.sbt" % "sbt-site"     % "1.3.1")
addSbtPlugin("com.eed3si9n"     % "sbt-unidoc"   % "0.4.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages"  % "0.6.2")
addSbtPlugin("io.get-coursier"  % "sbt-coursier" % "1.0.0-RC11")
addSbtPlugin("com.typesafe.sbt" % "sbt-git"      % "0.9.3")
