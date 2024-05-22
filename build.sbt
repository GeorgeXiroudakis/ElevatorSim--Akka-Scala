ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "elevatorSimulatorUsingSclaActors"
  )

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.20"
