lazy val commonSettings = Seq(
  organization := "com.ybrikman",
  version := "0.0.1",
  scalaVersion := "2.11.6",
  scalacOptions += "-feature",
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
)

lazy val bigPipe = (project in file("big-pipe"))
  .settings(commonSettings)
  .enablePlugins(SbtTwirl)
  .settings(
    name := "ping-play-big-pipe",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % play.core.PlayVersion.current,
      "com.typesafe.play" %% "play-iteratees" % play.core.PlayVersion.current,
      specs2 % Test
    )
  )

lazy val sampleApp = (project in file("sample-app"))
  .settings(commonSettings)
  .enablePlugins(PlayScala)
  .dependsOn(bigPipe)
  .settings(
    name := "ping-play-sample-app",
    play.twirl.sbt.Import.TwirlKeys.templateFormats ++= Map("stream" -> "com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamFormat"),
    play.twirl.sbt.Import.TwirlKeys.templateImports ++= Vector("com.ybrikman.ping.scalaapi.bigpipe.HtmlStream", "com.ybrikman.ping.scalaapi.bigpipe.HtmlStream._"),
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(
      ws,
      javaWs,
      "com.typesafe.play" %% "play-java" % play.core.PlayVersion.current,
      specs2 % Test
    )
  )









