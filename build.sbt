import Dependencies.*

ThisBuild / scalaVersion := "3.3.0"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = project
  .in(file("."))
  .settings(
    name := "jwt-auth-http4s",
    version := "0.1.0-SNAPSHOT",


    libraryDependencies ++= Seq(
      Library.cats,
      Library.catsEffect,
      Library.circe,
      Library.http4s,
      Library.http4sCLient,
      Library.http4sCirce,
      Library.http4sServer,
      Library.http4sDsl,
      Library.jwtCirce,
      Library.weaverCats,
      Library.weaverDiscipline,
      Library.weaverScalaCheck,
    ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )
