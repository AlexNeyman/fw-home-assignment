ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "flight-service",
    resolvers += "Sonatype OSS Snapshots" at
      "https://oss.sonatype.org/content/repositories/releases",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % "0.23.6",
      "org.http4s" %% "http4s-dsl" % "0.23.6",
      "org.http4s" %% "http4s-circe" % "0.23.6",
      "io.circe" %% "circe-core" % "0.14.1",
      "io.circe" %% "circe-generic" % "0.14.1",
      "io.circe" %% "circe-parser" % "0.14.1",
      "org.typelevel" %% "cats-effect" % "3.2.9",
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "com.storm-enroute" %% "scalameter" % "0.21",
    ),
    idePackagePrefix := Some("gpt"),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
  )

fork := true

outputStrategy := Some(StdoutOutput)

connectInput := true
