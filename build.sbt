import ReleaseTransformations._

// Used to publish this repo to Sonatype as per http://www.scala-sbt.org/release/docs/Using-Sonatype.html
lazy val publishSettings = Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  homepage := Some(url("https://github.com/brikis98/ping-play")),
  scmInfo := Some(ScmInfo(url("https://github.com/brikis98/ping-play"), "scm:git:git@github.com:brikis98/ping-play.git")),
  // The "developers" key does not get inserted into the POM correctly, so we have to use pomExtra and do it manually
  // developers := List(Developer("brikis98", "Yevgeniy Brikman", "jim@ybrikman.com", url("http://www.ybrikman.com"))),
  pomExtra := (
    <developers>
      <developer>
        <id>brikis98</id>
        <name>Yevgeniy Brikman</name>
        <url>http://www.ybrikman.com</url>
      </developer>
    </developers>
  ),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    ReleaseStep(action = Command.process("publishSigned", _)),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges
  )
)

// Settings shared by all the projects
lazy val commonSettings = Seq(
  organization := "com.ybrikman.ping",
  scalaVersion := "2.11.6",
  scalacOptions += "-feature",
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
) ++ publishSettings

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









