import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import sbt.Keys._

import com.atlassian.labs.gitstamp.GitStampPlugin._

name := """crawler"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    crossPaths := false,
    scalacOptions += "-language:postfixOps",
    scalaVersion := "2.11.8",
    javaOptions in run ++= Seq(
      "-XX:+HeapDumpOnOutOfMemoryError",
      "-XX:HeapDumpPath=/data/logs/crawler"
    ),
    Keys.fork in run := true
  )

javaOptions in Universal ++= Seq(
  "-J-XX:+HeapDumpOnOutOfMemoryError",
  "-J-XX:HeapDumpPath=/data/logs/crawler",
  "-Dhttp.port=8080"
)

scalaVersion := "2.11.8"

Seq( gitStampSettings: _* )

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "requirejs" % "2.1.15",
  "org.webjars" % "bootbox" % "4.4.0",
  "org.webjars" % "angular-ui-bootstrap" % "1.3.3",
  "org.webjars" % "angular-ui-select" % "0.17.1",
  "org.webjars.bower" % "angularjs" % "1.5.8",
  "org.webjars.bower" % "angular-resource" % "1.5.8",
  "org.webjars" % "angular-sanitize" % "1.3.0-beta.18",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.1" % "test",
  "com.typesafe.play" %% "play" % "2.5.4",
  "com.typesafe.play" %% "play-json" % "2.5.4",
  "net.ruippeixotog" %% "scala-scraper" % "2.0.0"
)

packageOptions += Package.MainClass("JettyLauncher")

enablePlugins(JavaAppPackaging)

// make sure RPM builder repacks JAR files
rpmBrpJavaRepackJars := true

// do not add Scala version to the packages
crossPaths := false

// disable publishing the main jar produced by `package`
publishArtifact in (Compile, packageBin) := false

// disable publishing the main API jar
publishArtifact in (Compile, packageDoc) := false

// disable publishing the main sources jar
publishArtifact in (Compile, packageSrc) := false

// disable publishing the test jar
publishArtifact in (Test, packageBin) := false

defaultLinuxInstallLocation := "/opt/crawler"

defaultLinuxLogsLocation := "/data/logs"

name in Universal := "crawler"

maintainer := "Svitovyda"

packageSummary := "Crawler"

packageDescription := """Crawler"""

name in Rpm := "play-254"

version in Rpm := version.value stripSuffix "-SNAPSHOT"

rpmRelease := {
  val time = LocalDateTime.now().format(
    DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
  )
  if (isSnapshot.value) s"$time" else "1"
}

publishMavenStyle := false

rpmVendor := "Svitovyda"

rpmUrl := Some("")

rpmGroup := Some("group")

rpmLicense := Some("Svitovyda")

publish <<= publish.dependsOn(publish in config("rpm"))
