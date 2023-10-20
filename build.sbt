ThisBuild / version := "0.1.0-SNAPSHOT"

scalacOptions ++= Seq(
  "-language:implicitConversions"
)

ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
     name := "weather",
     libraryDependencies ++= Seq(
         "com.typesafe.play" %% "play-json" % "2.10.0-RC9"
       , "com.lihaoyi" %% "requests" % "0.8.0"
       , "com.typesafe" % "config" % "1.4.2"
       , "org.postgresql" % "postgresql" % "42.6.0"
    )
  )
