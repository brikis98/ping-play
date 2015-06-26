name := "ping-play"

version := "1.0-SNAPSHOT"  

scalaVersion := "2.11.6"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

// Add a new template type for streaming templates
play.twirl.sbt.Import.TwirlKeys.templateFormats ++= Map("stream" -> "ui.HtmlStreamFormat")

// Add some useful default imports for streaming templates
play.twirl.sbt.Import.TwirlKeys.templateImports ++= Vector("_root_.ui.HtmlStream", "_root_.ui.HtmlStream._")

libraryDependencies ++= Seq(
  ws,
  javaWs,
  "com.typesafe.play" %% "play-java" % play.core.PlayVersion.current,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

scalacOptions += "-feature"

routesGenerator := InjectedRoutesGenerator
