ThisBuild / licenses += "ISC" -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme := Some("semver-spec")

lazy val highlighter = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("."))
  .settings(
    name := "highlighter",
    version := "0.1.1-pre.6",
    scalaVersion := "2.13.6",
    scalacOptions ++=
      Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-language:postfixOps",
        "-language:implicitConversions",
        "-language:existentials",
        "-language:dynamics",
        "-Xasync"
      ),
    organization := "io.github.edadma",
    githubOwner := "edadma",
    githubRepository := name.value,
    mainClass := Some(s"${organization.value}.${name.value}.Main"),
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.10" % "test",
    libraryDependencies += "org.scala-lang.modules" %%% "scala-parser-combinators" % "2.0.0",
    libraryDependencies += "com.lihaoyi" %%% "pprint" % "0.6.6",
    libraryDependencies ++= Seq(
      "io.github.edadma" %%% "backslash" % "0.1.0"
    ),
    publishMavenStyle := true,
    Test / publishArtifact := false,
    licenses := Seq("ISC" -> url("https://opensource.org/licenses/ISC"))
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided",
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0" % "test",
  )
  .
  //  nativeSettings(
  //    nativeLinkStubs := true
  //  ).
  jsSettings(
  jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
  //    Test / scalaJSUseMainModuleInitializer := true,
  //    Test / scalaJSUseTestModuleInitializer := false,
  Test / scalaJSUseMainModuleInitializer := false,
  Test / scalaJSUseTestModuleInitializer := true,
  scalaJSUseMainModuleInitializer := true
)
