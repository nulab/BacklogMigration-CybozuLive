
name := "backlog-migration-cybozulive"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.4"

libraryDependencies ++= {
  val catsVersion = "1.0.1"
  val slickVersion = "3.2.2"
  Seq(
    "org.typelevel"         %% "cats-core"        % catsVersion,
    "org.typelevel"         %% "cats-free"        % catsVersion,
    "org.typelevel"         %% "cats-effect"      % "0.9",
    "com.typesafe"          %  "config"           % "1.3.3",
    "com.github.scopt"      %% "scopt"            % "3.7.0",
    "org.fusesource.jansi"  %  "jansi"            % "1.17",
    "com.typesafe.slick"    %% "slick"            % slickVersion,
    "com.typesafe.slick"    %% "slick-hikaricp"   % slickVersion,
    "org.xerial"            %  "sqlite-jdbc"      % "3.8.7",
    "ch.qos.logback"        %  "logback-classic"  % "1.2.3",
    "com.osinka.i18n"       %% "scala-i18n"       % "1.0.2",
    "io.monix"              %% "monix-reactive"   % "3.0.0-M1",
    "io.monix"              %% "monix-nio"        % "0.0.2",
    "org.apache.commons"    %  "commons-csv"      % "1.5",
    "org.scalatest"         %% "scalatest"        % "3.0.1"       % Test
  )
}
