
libraryDependencies ++= {
  val slickVersion = "3.2.2"
  Seq(
    "com.typesafe.slick"  %% "slick"          % slickVersion,
    "com.typesafe.slick"  %% "slick-hikaricp" % slickVersion,
    "org.slf4j"           %  "slf4j-nop"      % "1.6.4",
    "org.xerial"          %  "sqlite-jdbc"    % "3.8.7"
  )
}