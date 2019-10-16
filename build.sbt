name := "akka-master-worker-template"

version := "0.1"

scalaVersion := "2.12.4"

val akkaVersion = "2.5.25"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %%  "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %%  "akka-http" % "10.1.5"
)