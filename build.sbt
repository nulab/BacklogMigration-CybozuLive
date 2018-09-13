
name := "backlog-migration-cybozulive"

lazy val commonSettings = Seq(
  version := "1.1.2-SNAPSHOT",
  scalaVersion := "2.12.6"
)

lazy val backlogMigrationCommon = (project in file("modules/common"))
  .settings(commonSettings)

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
      val catsVersion = "1.3.1"
      val slickVersion = "3.2.3"
      val monixVersion = "3.0.0-RC1"
      val akkaHttpVersion = "10.1.5"
      Seq(
        "com.github.scopt"      %% "scopt"            % "3.7.0",
        "org.typelevel"         %% "cats-core"        % catsVersion,
        "org.typelevel"         %% "cats-free"        % catsVersion,
        "com.typesafe.slick"    %% "slick"            % slickVersion,
        "com.typesafe.slick"    %% "slick-hikaricp"   % slickVersion,
        "org.xerial"            %  "sqlite-jdbc"      % "3.23.1",
        "io.monix"              %% "monix"            % monixVersion,
        "io.monix"              %% "monix-reactive"   % monixVersion,
        "io.monix"              %% "monix-execution"  % monixVersion,
        "io.monix"              %% "monix-eval"       % monixVersion,
        "com.typesafe.akka"     %% "akka-http"        % akkaHttpVersion,
        "org.apache.commons"    %  "commons-csv"      % "1.5",
        "org.scalatest"         %% "scalatest"        % "3.0.5"       % Test
      )
    },
    assemblyJarName in assembly := {
      s"${name.value}-${version.value}.jar"
    }
  )
  .dependsOn(backlogMigrationCommon, backlogMigrationImporter)
  .dependsOn(backlog4s_core, backlog4s_akka)