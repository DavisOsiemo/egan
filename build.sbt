val ATSnapshots = "AT Snapshots" at "https://deino.at-internal.com/repository/maven-snapshots/"
val ATReleases  = "AT Releases"  at "https://deino.at-internal.com/repository/maven-releases/"

lazy val sharedSettings = Seq(
  organization := "com.africasTalking",
  version      := "1.0",
  scalaVersion := "2.12.10",
  resolvers    ++= Seq(
    ATSnapshots,
    ATReleases,
    "Typesafe repository releases" at "https://repo.typesafe.com/typesafe/releases/",
    "Confluent Maven Repository" at "https://packages.confluent.io/maven/"
  ),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case "META-INF/io.netty.versions.properties" => MergeStrategy.first
    case "logback.xml"                           => MergeStrategy.first
    case PathList("io", "netty", xs @ _*)        => MergeStrategy.last
    case PathList("org", "apache", xs @ _*)      => MergeStrategy.first
    case PathList("org", "slf4j", xs @ _*)       => MergeStrategy.first
    case x                                       =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

val akkaVersion      = "2.6.0"
val akkaHttpVersion  = "10.1.10"
val scalaTestVersion = "3.1.0"

val testDependencies = Seq(
  "org.scalactic"     %% "scalactic"           % scalaTestVersion % Test,
  "org.scalatest"     %% "scalatest"           % scalaTestVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion      % Test,
  "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion  % Test
)

lazy val egan = (project in file("."))
  .settings(sharedSettings)
  .aggregate(core, handler, web)

lazy val core = (project in file("core")).
  settings(
    sharedSettings,
    libraryDependencies ++= testDependencies,
    libraryDependencies ++= Seq(
      "commons-daemon"          %  "commons-daemon"               % "1.2.2",
      "ch.qos.logback"          %  "logback-classic"              % "1.2.3",
      "com.typesafe.akka"       %% "akka-actor"                   % akkaVersion,
      "com.typesafe.akka"       %% "akka-http"                    % akkaHttpVersion,
      "com.typesafe.akka"       %% "akka-stream"                  % akkaVersion,
      "com.typesafe.akka"       %% "akka-slf4j"                   % akkaVersion,
      "com.typesafe.akka"       %% "akka-http-spray-json"         % akkaHttpVersion,
      "io.atlabs"               %% "horus-core"                   % "0.1.20",
      "com.github.mauricio"     %% "mysql-async"                  % "0.2.21",
      "ch.qos.logback"          %  "logback-classic"              % "1.2.1",
      "ch.qos.logback"          %  "logback-core"                 % "1.2.1",
      "com.github.etaty"        %% "rediscala"                    % "1.8.0",
      "org.lz4"                 %  "lz4-java"                     % "1.6.0",
      "io.netty"                %  "netty-transport-native-epoll" % "4.0.17.Final",
      "org.scala-lang"          %  "scala-reflect"                % scalaVersion.value
    )
  )

lazy val handler = (project in file("handler")).
  settings(
    sharedSettings,
    libraryDependencies ++= testDependencies
  ).dependsOn(core)

lazy val web = (project in file("web")).
  settings(
    sharedSettings,
    libraryDependencies ++= testDependencies
  ).dependsOn(core, handler)

