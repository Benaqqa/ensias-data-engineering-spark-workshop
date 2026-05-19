ThisBuild / scalaVersion := "2.12.15"

val sparkVersion = "3.5.1"
val deltaVersion = "3.2.0"
val scoptVersion = "4.1.0"

// -----------------------------------------------------------------------------
// Assembly plugin import
// -----------------------------------------------------------------------------
import sbtassembly.AssemblyPlugin.autoImport._

lazy val root = (project in file("."))
  .settings(

    name := "ensias-de-workshop-spark",
    version := "1.0.0",

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    libraryDependencies ++= Seq(

      // Spark (provided by spark-submit runtime)
      "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
      "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
      "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided,

      // Delta Lake
      "io.delta" %% "delta-spark" % deltaVersion,

      // CLI
      "com.github.scopt" %% "scopt" % scoptVersion,

      // Logging (IMPORTANT: align versions with Spark)
      "org.slf4j" % "slf4j-api" % "2.0.13",
      "ch.qos.logback" % "logback-classic" % "1.2.13",

      // Tests
      "org.scalatest" %% "scalatest" % "3.2.18" % Test
    ),

    // -------------------------------------------------------------------------
    // Package metadata
    // -------------------------------------------------------------------------

    Compile / packageBin / packageOptions +=
      Package.ManifestAttributes(
        "Implementation-Title" -> name.value,
        "Implementation-Version" -> version.value,
        "Scala-Version" -> scalaVersion.value,
        "Created-By" -> "sbt"
      ),

    // -------------------------------------------------------------------------
    // sbt-assembly settings (UBER JAR)
    // -------------------------------------------------------------------------

    assembly / assemblyJarName := s"${name.value}-${version.value}-assembly.jar",

    assembly / test := {},

    // IMPORTANT: better merge strategy for Spark + Delta
    assembly / assemblyMergeStrategy := {
        case PathList("META-INF", _ @ _*) => MergeStrategy.discard
        case "reference.conf"             => MergeStrategy.concat
        case _                            => MergeStrategy.first
    }
  )