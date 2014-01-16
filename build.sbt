name := "ping-conference"

version := "1.0-SNAPSHOT"  

play.Project.playScalaSettings

offline := true

overrideBuildResolvers := false

play.Keys.templatesTypes ++= Map("stream" -> "ui.HtmlStreamFormat")

play.Keys.templatesImport ++= Vector("_root_.ui.HtmlStream", "_root_.ui.HtmlStream._", "_root_.ui.StaticContent")

