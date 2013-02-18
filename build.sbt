name := "search-terms"

scalaVersion := "2.10.0"

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.3.27"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"

libraryDependencies += "com.typesafe" % "config" % "1.0.0"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "0.2.0"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.0-M7"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.1.0"

libraryDependencies +="com.github.theon" %% "scala-uri" % "0.3.2"

javaOptions += "-Xmx8G"
