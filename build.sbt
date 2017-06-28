name := "akka-cluster-playground"

organization := "akka-cluster-playground"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.2"

val AkkaVersion = "2.5.3"

resolvers += Resolver.typesafeRepo("releases")

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion
