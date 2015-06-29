lazy val commonSettings = Seq(
  organization := "com.ybrikman.ping-play",
  version := "0.0.1",
  scalaVersion := "2.11.6",
  scalacOptions += "-feature",
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
)

// You must added these settings to your Play app to be able to use .scala.stream templates for BigPipe-style streaming
lazy val bigPipeSettings = Seq(
  TwirlKeys.templateFormats ++= Map("stream" -> "com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamFormat"),
  TwirlKeys.templateImports ++= Vector("com.ybrikman.ping.scalaapi.bigpipe.HtmlStream")
)

// The BigPipe library
lazy val bigPipe = (project in file("big-pipe"))
  .settings(commonSettings)
  .enablePlugins(SbtTwirl)
  .settings(
    name := "big-pipe",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % play.core.PlayVersion.current,
      "com.typesafe.play" %% "play-iteratees" % play.core.PlayVersion.current,
      specs2 % Test
    )
  )

// Some shared code for the sample apps
lazy val sampleAppCommon = (project in file("sample-app-common"))
  .settings(commonSettings)
  .settings(bigPipeSettings)
  .enablePlugins(SbtTwirl)
  .dependsOn(bigPipe)
  .settings(
    name := "sample-app-common",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % play.core.PlayVersion.current,
      specs2 % Test
    )
  )

// The Scala sample app
lazy val sampleAppScala = (project in file("sample-app-scala"))
  .settings(commonSettings)
  .settings(bigPipeSettings)
  .enablePlugins(PlayScala)
  .dependsOn(bigPipe, sampleAppCommon)
  .settings(
    name := "sample-app-scala",
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(
      ws,
      specs2 % Test
    ),
    // These two settings are to ensure the test servers don't use the same port if they happen to run in parallel
    fork in Test := true,
    javaOptions in Test += "-Dtestserver.port=19111"
  )

// The Java sample app
lazy val sampleAppJava = (project in file("sample-app-java"))
  .settings(commonSettings)
  .settings(bigPipeSettings)
  .enablePlugins(PlayJava)
  .dependsOn(bigPipe, sampleAppCommon)
  .settings(
    name := "sample-app-java",
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(
      javaWs,
      "com.typesafe.play" %% "play-java" % play.core.PlayVersion.current,
      specs2 % Test
    ),
    // These two settings are to ensure the test servers don't use the same port if they happen to run in parallel
    fork in Test := true,
    javaOptions in Test += "-Dtestserver.port=19222"
  )









