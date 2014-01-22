name := "ping-conference"

version := "1.0-SNAPSHOT"  

play.Project.playScalaSettings

// Add a new template type for streaming templates
play.Keys.templatesTypes ++= Map("stream" -> "ui.HtmlStreamFormat")

// Add some useful default imports for streaming templates
play.Keys.templatesImport ++= Vector("_root_.ui.HtmlStream", "_root_.ui.HtmlStream._", "_root_.ui.StaticContent")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-java" % play.core.PlayVersion.current
)