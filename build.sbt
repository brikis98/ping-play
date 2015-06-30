import ReleaseTransformations._

// The BigPipe library
lazy val bigPipe = (project in file("big-pipe"))
  .settings(commonSettings ++ publishSettings ++ bigPipeSettings)
  .enablePlugins(SbtTwirl)

// Some shared code for the sample apps
lazy val sampleAppCommon = (project in file("sample-app-common"))
  .settings(commonSettings ++ streamingTemplateSettings ++ sampleAppCommonSettings)
  .enablePlugins(SbtTwirl)
  .dependsOn(bigPipe)

// The Scala sample app
lazy val sampleAppScala = (project in file("sample-app-scala"))
  .settings(commonSettings ++ streamingTemplateSettings ++ sampleAppScalaSettings)
  .enablePlugins(PlayScala)
  .dependsOn(bigPipe, sampleAppCommon)

// The Java sample app
lazy val sampleAppJava = (project in file("sample-app-java"))
  .settings(commonSettings ++ streamingTemplateSettings ++ sampleAppJavaSettings)
  .settings()
  .enablePlugins(PlayJava)
  .dependsOn(bigPipe, sampleAppCommon)

// Settings shared by all the projects
lazy val commonSettings = Seq(
  organization := "com.ybrikman.ping",
  scalaVersion := "2.11.6",
  scalacOptions += "-feature",
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
)

// Settings specific to the bigPipe project
lazy val bigPipeSettings = Seq(
  name := "big-pipe",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % play.core.PlayVersion.current,
    "com.typesafe.play" %% "play-iteratees" % play.core.PlayVersion.current,
    specs2 % Test
  )
)

// Settings specific to the sampleAppCommon project
lazy val sampleAppCommonSettings = Seq(
  name := "sample-app-common",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % play.core.PlayVersion.current,
    specs2 % Test
  )
)

// Settings specific to the sampleAppScala project
lazy val sampleAppScalaSettings = Seq(
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

// Settings specific to the sampleAppJava project
lazy val sampleAppJavaSettings = Seq(
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

// You must added these settings to your Play app to be able to use .scala.stream templates for BigPipe-style streaming
lazy val streamingTemplateSettings = Seq(
  TwirlKeys.templateFormats ++= Map("stream" -> "com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamFormat"),
  TwirlKeys.templateImports ++= Vector("com.ybrikman.ping.scalaapi.bigpipe.HtmlStream")
)

lazy val updateVersionNumberInReadme = taskKey[String]("Updates the version number in the README to the current version")

// Used to publish the bigPipe project to Sonatype as per http://www.scala-sbt.org/release/docs/Using-Sonatype.html
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
  updateVersionNumberInReadme := {
    val readmePath = baseDirectory.value / ".." / "README.md"
    val readmeText = IO.read(readmePath)
    val releaseVersion = version.value

    val DependencyRegex = """("com.ybrikman.ping" %% "big-pipe" % ")(.+?)(")""".r
    val updatedReadmeText = DependencyRegex.replaceAllIn(readmeText, "$1" + releaseVersion + "$3")
    IO.write(readmePath, updatedReadmeText)

    releaseVersion
  },
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    releaseStepTask(updateVersionNumberInReadme),
    commitReleaseVersion,
    tagRelease,
    ReleaseStep(action = Command.process("publishSigned", _)),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges
  )
)





