
resolvers += Resolver.bintrayRepo("zamblauskas", "maven")

libraryDependencies ++= Seq(
  "zamblauskas" %% "scala-csv-parser" % "0.11.4"
)