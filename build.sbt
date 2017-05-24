name := "geo-stream-rdf"

organization in ThisBuild := "Cruis-R"
version in ThisBuild := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.11" // "2.12.2"
javacOptions in ThisBuild := Seq("-source","1.8", "-target","1.8")
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-explaintypes", "-language:_", "-Xlint:_")

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.3"

