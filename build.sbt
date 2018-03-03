
name := "backlog-migration-cybozulive"

lazy val projectVersion = "0.1.0-SNAPSHOT"

lazy val commonSettings = Seq(
  version := projectVersion,
  scalaVersion := "2.12.4",
  libraryDependencies ++= {
    val catsVersion = "1.0.1"
    Seq(
      "org.typelevel"   %% "cats-core"        % catsVersion,
      "org.typelevel"   %% "cats-free"        % catsVersion,
      "org.typelevel"   %% "cats-effect"      % "0.9",
      "ch.qos.logback"  %  "logback-classic"  % "1.2.3",
      "com.osinka.i18n" %% "scala-i18n"       % "1.0.2"
    )
  }
)

lazy val core = (project in file("modules/core"))
  .settings(commonSettings)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe"          %  "config"     % "1.3.3",
      "com.github.scopt"      %% "scopt"      % "3.7.0",
      "org.fusesource.jansi"  %  "jansi"      % "1.17"
    )
  )
  .dependsOn(core)


