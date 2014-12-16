import play.PlayScala

val originalJvmOptions = sys.process.javaVmArguments.filter(
  a => Seq("-Xmx", "-Xms", "-XX").exists(a.startsWith)
)

val baseSettings = Seq(
  scalaVersion := "2.11.4",
  scalacOptions ++= (
    "-deprecation" ::
    "-unchecked" ::
    "-Xlint" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    Nil
  ),
  watchSources ~= { _.filterNot(f => f.getName.endsWith(".swp") || f.getName.endsWith(".swo") || f.isDirectory) },
  javaOptions ++= originalJvmOptions,
  ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
  shellPrompt := { state =>
    val branch = if(file(".git").exists){
      "git branch".lines_!.find{_.head == '*'}.map{_.drop(1)}.getOrElse("")
    }else ""
    Project.extract(state).currentRef.project + branch + " > "
  },
  updateOptions ~= {_.withCachedResolution(true)},
  incOptions := incOptions.value.withNameHashing(true),
  resolvers ++= Seq(Opts.resolver.sonatypeReleases)
)

lazy val root = Project(
  "mon", file(".")
).enablePlugins(PlayScala).settings(
  baseSettings: _*
).settings(
  libraryDependencies ++= Seq(
    "uk.gov.hmrc" %% "simple-reactivemongo" % "2.1.0"
//    "com.typesafe.play" % "play-json" % "2.4.0-M2"
  )
)

