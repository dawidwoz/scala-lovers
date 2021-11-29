val scala3Version = "3.1.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "homework",
    version := "0.1.0-SNAPSHOT",

   scalaVersion := scala3Version,
    scalacOptions ++= Seq ("-deprecation", "-feature", "-Xfatal-warnings"),

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.9" % Test,
      "org.scalatestplus" %% "scalacheck-1-15" % "3.2.10.0" % Test
    )
  )
