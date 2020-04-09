
lazy val akkaVersion = "2.6.4"
val cassandraPluginVersion = "0.102"

lazy val `document-validator` = (project in file("."))
  .aggregate(validator)

lazy val validator = (project in file("validator"))
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    name := "document-validator",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      "com.lightbend.akka" %% "akka-stream-alpakka-file" % "1.1.2",
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "1.0.6" excludeAll(ExclusionRule(organization = "com.typesafe.akka")),
      "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.6" excludeAll(ExclusionRule(organization = "com.typesafe.akka")),
      "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % "10.1.11",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11",
      "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.5.3",
      "org.postgresql" % "postgresql" % "42.1.4",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      // test dependencies
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "commons-io" % "commons-io" % "2.4" % Test)
  )
  .settings(commonSettings)
  .settings(dockerSettings)

lazy val commonSettings = Seq(
  organization := "com.lightbend",
  scalaVersion := "2.13.1",
  crossScalaVersions := Vector(scalaVersion.value),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ),
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
  scalafmtOnCompile := true
)

def dockerSettings = Seq(
  dockerUpdateLatest := true,
  dockerBaseImage := getDockerBaseImage(),
  dockerUsername := sys.props.get("docker.username"),
  dockerRepository := Some("thinkmorestupidless"),
  dockerExposedPorts := Seq(8080, 8558, 2550, 9000, 9001)
)

def getDockerBaseImage(): String = sys.props.get("java.version") match {
  case Some(v) if v.startsWith("11") => "adoptopenjdk/openjdk11"
  case _ => "adoptopenjdk/openjdk8"
}

version in ThisBuild ~= (_.replace('+', '-'))
