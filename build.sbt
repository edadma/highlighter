ThisBuild / licenses += "ISC" -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme := Some("semver-spec")

publish / skip := true

lazy val highlighter = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("."))
  .settings(
    name := "highlighter",
    version := "0.1.1-pre.10",
    scalaVersion := "3.1.3",
    scalacOptions ++=
      Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-language:postfixOps",
        "-language:implicitConversions",
        "-language:existentials",
        "-language:dynamics",
      ),
    organization := "io.github.edadma",
    githubOwner := "edadma",
    githubRepository := name.value,
    mainClass := Some(s"${organization.value}.${name.value}.Main"),
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.12" % "test",
    libraryDependencies += "org.scala-lang.modules" %%% "scala-parser-combinators" % "2.1.1",
    libraryDependencies ++= Seq(
      "com.github.scopt" %%% "scopt" % "4.1.0",
      "com.lihaoyi" %%% "pprint" % "0.7.3",
    ),
    libraryDependencies ++= Seq(
      "io.github.edadma" %%% "mustache" % "0.1.13"
    ),
    publishMavenStyle := true,
    Test / publishArtifact := false,
    licenses := Seq("ISC" -> url("https://opensource.org/licenses/ISC"))
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided"
  )
  .nativeSettings(
    nativeLinkStubs := true
  )
  .jsSettings(
    jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    //    Test / scalaJSUseMainModuleInitializer := true,
    //    Test / scalaJSUseTestModuleInitializer := false,
    Test / scalaJSUseMainModuleInitializer := false,
    Test / scalaJSUseTestModuleInitializer := true,
    scalaJSUseMainModuleInitializer := true
  )
