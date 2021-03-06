name := "My Project"
 
version := "1.0"
 
scalaVersion := "2.11.8"
 
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.9-RC2",
  "com.typesafe.akka" %% "akka-remote" % "2.4.9-RC2"
)

libraryDependencies += "com.github.etaty" %% "rediscala" % "1.6.0"