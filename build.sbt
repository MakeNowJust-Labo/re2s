Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / githubOwner := "MakeNowJust-Labo"
ThisBuild / githubRepository := "re2s"

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-deprecation",
  "-Wunused",
  "-language:implicitConversions"
)

// Scalafix config:
ThisBuild / scalafixScalaBinaryVersion := "2.13"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
ThisBuild / scalafixDependencies += "com.github.vovapolu" %% "scaluzzi" % "0.1.17"

lazy val root = project
  .in(file("."))
  .settings(
    organization := "codes.quine.labo",
    name := "re2s",
    version := "0.1.1-SNAPSHOT",
    console / initialCommands := """
      |import codes.quine.labo.re2s._
      """.stripMargin,
    Compile / console / scalacOptions -= "-Wunused",
    // Set URL mapping of scala standard API for Scaladoc.
    apiMappings ++= scalaInstance.value.libraryJars
      .filter(file => file.getName.startsWith("scala-library") && file.getName.endsWith(".jar"))
      .map(_ -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
      .toMap,
    // Dependencies:
    libraryDependencies += "com.google.re2j" % "re2j" % "1.5",
    // Settings for test:
    libraryDependencies += "io.monix" %% "minitest" % "2.9.2" % Test,
    testFrameworks += new TestFramework("minitest.runner.Framework"),
    doctestTestFramework := DoctestTestFramework.Minitest,
    doctestMarkdownEnabled := true,
    // Surpress warnings in doctest generated files.
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.2" cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % "1.7.2" % Provided cross CrossVersion.full
    ),
    scalacOptions += "-P:silencer:globalFilters=toVoid is never used"
  )
