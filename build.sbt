
name := "backlog-migration-cybozulive"

lazy val projectVersion = "0.1.0-SNAPSHOT"

lazy val commonSettings = Seq(
  version := projectVersion,
  scalaVersion := "2.12.4",
  libraryDependencies ++= {
    val catsVersion = "1.0.1"
    Seq(
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-free" % catsVersion
    )
  }
)

lazy val core = (project in file("modules/core"))
  .settings(commonSettings)

lazy val cli = (project in file("modules/cli"))
  .settings(commonSettings)
  .dependsOn(core)

