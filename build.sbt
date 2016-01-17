name := "id3reader"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/"

libraryDependencies ++= Seq(
  "org.scodec" %% "scodec-bits" % "1.0.12",
  "org.scodec" %% "scodec-core" % "1.8.3"
)
