package com.ensias

import io.delta.sql.DeltaSparkSessionExtension
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.streaming.StreamingQuery
import org.slf4j.{Logger, LoggerFactory}

abstract class SparkApp {
  /**
  * Base Spark application abstraction.
  *
  * Features:
  * - Delta Lake support
  * - Structured Streaming support
  * - SLF4J logging
  */

  // ---------------------------------------------------------------------------
  // Logger
  // ---------------------------------------------------------------------------

  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  // ---------------------------------------------------------------------------
  // Required implementations
  // ---------------------------------------------------------------------------

  def appName: String

  def run(args: Array[String]): Unit

  // ---------------------------------------------------------------------------
  // Optional overrides
  // ---------------------------------------------------------------------------

  def master: String = sys.env.getOrElse("SPARK_MASTER", "local[*]")

  def isStreamingProcessing: Boolean = false
  
  def enableHiveSupport: Boolean = false

  def logLevel: String = "INFO"

  def extraSparkConfigs: Map[String, String] = Map.empty

  protected val spark: SparkSession = buildSparkSession()

  // ---------------------------------------------------------------------------
  // Main entrypoint
  // ---------------------------------------------------------------------------

  final def main(args: Array[String]): Unit = {
    try {
      logStartup()
      run(args)
      if (isStreamingProcessing) waitForStreamingQueries()
      logger.info(s"Application completed successfully: $appName")
    } catch {
      case e: Throwable =>
        logger.error(s"Application failed: ${e.getMessage}", e)
        throw e
    }
  }

  // ---------------------------------------------------------------------------
  // Spark Session Builder
  // ---------------------------------------------------------------------------

  private def buildSparkSession(): SparkSession = {

    logger.info(s"Creating SparkSession for application: $appName")

    val builder = SparkSession
      .builder()
      .appName(appName)
      .master(master)
      // Delta Lake
      .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
      .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
      // General Spark tuning defaults
      .config(
        "spark.serializer",
        "org.apache.spark.serializer.KryoSerializer"
      )
      .config(
        "spark.sql.session.timeZone",
        "UTC"
      )

    // User-provided extra configs
    extraSparkConfigs.foreach {
      case (key, value) =>
        logger.info(s"Applying Spark config: $key=$value")
        builder.config(key, value)
    }
    if (enableHiveSupport) {
      logger.info("Hive support enabled")
      builder.enableHiveSupport()
    }
    val spark = builder.getOrCreate()
    spark.sparkContext.setLogLevel(logLevel)
    logger.info("SparkSession created successfully")
    spark
  }

  // ---------------------------------------------------------------------------
  // Streaming Support
  // ---------------------------------------------------------------------------

  protected def waitForStreamingQueries(queryTimeout: Int = 0 ): Unit = {

    val streams = spark.streams.active
    if (streams.nonEmpty) {
      logger.info(
        s"Detected ${streams.length} active streaming query(ies)"
      )
      if (queryTimeout > 0) {
        streams.foreach(_.awaitTermination(queryTimeout))
      } else {
        streams.foreach(_.awaitTermination())
      }
    } else {
      logger.info("No active streaming queries detected")
    }
  }

  // ---------------------------------------------------------------------------
  // Logging
  // ---------------------------------------------------------------------------

  protected def logStartup(): Unit = {

    logger.info(
      s"""
         |========================================================
         | Starting Spark Application
         |--------------------------------------------------------
         | App Name : $appName
         | Master   : $master
         | LogLevel : $logLevel
         |========================================================
         |""".stripMargin
    )
  }
}