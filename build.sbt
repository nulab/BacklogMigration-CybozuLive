
name := "backlog-migration-cybozulive"

lazy val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.4",
  libraryDependencies ++= Seq(
    "org.fusesource.jansi"  %  "jansi"            % "1.17",
    "com.osinka.i18n"       %% "scala-i18n"       % "1.0.2",
    "ch.qos.logback"        %  "logback-classic"  % "1.2.3",
    "com.typesafe"          %  "config"           % "1.3.3",
  )
)

lazy val backlogMigrationCommon = (project in file("modules/common"))
  .settings(commonSettings)
  .settings(
    unmanagedBase := baseDirectory.value / "libs",
    libraryDependencies ++= Seq(
      "com.google.inject"     %  "guice"          % "4.1.0",
      "io.spray"              %% "spray-json"     % "1.3.2",
      "com.mixpanel"          %  "mixpanel-java"  % "1.4.4",
      "net.codingwell"        %% "scala-guice"    % "4.1.0",
      "com.netaporter"        %% "scala-uri"      % "0.4.16",
      "com.github.pathikrit"  %% "better-files"   % "3.4.0"
    )
  )

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= {
      val catsVersion = "1.1.0"
      val slickVersion = "3.2.2"
      Seq(
        "org.typelevel"         %% "cats-core"        % catsVersion,
        "org.typelevel"         %% "cats-free"        % catsVersion,
        "org.typelevel"         %% "cats-effect"      % "0.9",
        "com.github.scopt"      %% "scopt"            % "3.7.0",
        "com.typesafe.slick"    %% "slick"            % slickVersion,
        "com.typesafe.slick"    %% "slick-hikaricp"   % slickVersion,
        "org.xerial"            %  "sqlite-jdbc"      % "3.8.7",
        "io.monix"              %% "monix-reactive"   % "3.0.0-RC1",
        "io.monix"              %% "monix-nio"        % "0.0.3",
        "org.apache.commons"    %  "commons-csv"      % "1.5",
        "org.scalatest"         %% "scalatest"        % "3.0.1"       % Test
      )
    }
  )
  .dependsOn(backlogMigrationCommon)