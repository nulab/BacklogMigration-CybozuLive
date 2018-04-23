
name := "backlog-migration-cybozulive"

lazy val commonSettings = Seq(
  version := "0.1.0a5-SNAPSHOT",
  scalaVersion := "2.12.5",
  libraryDependencies ++= Seq(
    "org.fusesource.jansi"  %  "jansi"            % "1.17",
    "com.osinka.i18n"       %% "scala-i18n"       % "1.0.2",
    "ch.qos.logback"        %  "logback-classic"  % "1.2.3",
    "com.typesafe"          %  "config"           % "1.3.3",
    "org.scalatest"         %% "scalatest"        % "3.0.5"  % Test
  )
)

lazy val backlogMigrationCommon = (project in file("modules/common"))
  .settings(commonSettings)
  .settings(
    unmanagedBase := baseDirectory.value / "libs",
    libraryDependencies ++= Seq(
      "com.google.inject"     %  "guice"          % "4.2.0",
      "io.spray"              %% "spray-json"     % "1.3.4",
      "com.mixpanel"          %  "mixpanel-java"  % "1.4.4",
      "net.codingwell"        %% "scala-guice"    % "4.2.0",
      "com.netaporter"        %% "scala-uri"      % "0.4.16",
      "com.github.pathikrit"  %% "better-files"   % "3.4.0"
    )
  )

lazy val backlogMigrationImporter = (project in file("modules/importer"))
  .settings(commonSettings)
  .dependsOn(backlogMigrationCommon)

lazy val backlog4s_core = (project in file("modules/backlog4s/backlog4s-core"))
  .settings(commonSettings)

lazy val backlog4s_akka = (project in file("modules/backlog4s/backlog4s-akka"))
  .settings(commonSettings)
  .dependsOn(backlog4s_core)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= {
      val catsVersion = "1.1.0"
      val slickVersion = "3.2.3"
      val monixVersion = "3.0.0-RC1"
      Seq(
        "com.github.scopt"      %% "scopt"            % "3.7.0",
        "org.typelevel"         %% "cats-core"        % catsVersion,
        "org.typelevel"         %% "cats-free"        % catsVersion,
        "com.typesafe.slick"    %% "slick"            % slickVersion,
        "com.typesafe.slick"    %% "slick-hikaricp"   % slickVersion,
        "org.xerial"            %  "sqlite-jdbc"      % "3.21.0",
        "io.monix"              %% "monix"            % monixVersion,
        "io.monix"              %% "monix-reactive"   % monixVersion,
        "io.monix"              %% "monix-execution"  % monixVersion,
        "io.monix"              %% "monix-eval"       % monixVersion,
        "org.apache.commons"    %  "commons-csv"      % "1.5"
      )
    },
    assemblyJarName in assembly := {
      s"${name.value}-${version.value}.jar"
    }
  )
  .dependsOn(backlogMigrationCommon, backlogMigrationImporter)
  .dependsOn(backlog4s_core, backlog4s_akka)