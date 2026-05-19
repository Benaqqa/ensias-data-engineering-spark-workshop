package com.ensias.utils

import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkIOUtils {

  // ===========================================================================
  // READ CONFIG
  // ===========================================================================

  case class ReadConfig(
    format: String,
    path: String,
    options: Map[String, String] = Map.empty,
    where: Option[String] = None
  )

  // ===========================================================================
  // WRITE CONFIG (DELTA-AWARE)
  // ===========================================================================

  case class WriteConfig(
    format: String,
    path: String,
    mode: String = "overwrite",
    partitionBy: Seq[String] = Seq.empty,
    replaceWhere: Option[String] = None,
    overwriteSchema: Boolean = false,
    mergeSchema: Boolean = false,
    options: Map[String, String] = Map.empty
  )

  // ===========================================================================
  // READ (DATAFRAME ONLY)
  // ===========================================================================

  def readDataFrame(
    spark: SparkSession,
    config: ReadConfig
  ): DataFrame = {

    val baseReader = config.options.foldLeft(
      spark.read.format(config.format)
    ) {
      case (reader, (k, v)) => reader.option(k, v)
    }

    val df = baseReader.load(config.path)

    config.where match {
      case Some(condition) => df.where(condition)
      case None            => df
    }
  }

  // ===========================================================================
  // WRITE (DATAFRAME ONLY)
  // ===========================================================================

  def writeDataFrame(
    df: DataFrame,
    config: WriteConfig
  ): Unit = {

    val baseWriter = df.write
      .format(config.format)
      .mode(config.mode)

    val partitionedWriter =
      if (config.partitionBy.nonEmpty)
        baseWriter.partitionBy(config.partitionBy: _*)
      else
        baseWriter

    val withOptions = config.options.foldLeft(partitionedWriter) {
      case (writer, (k, v)) => writer.option(k, v)
    }

    val finalWriter = config.format.toLowerCase match {

      case "delta" =>
        val deltaWriter = withOptions.format("delta")

        val withReplaceWhere =
          config.replaceWhere match {
            case Some(cond) => deltaWriter.option("replaceWhere", cond)
            case None       => deltaWriter
          }

        (config.overwriteSchema, config.mergeSchema) match {
          case (true, _) =>
            withReplaceWhere.option("overwriteSchema", "true")
          case (_, true) =>
            withReplaceWhere.option("mergeSchema", "true")
          case _ =>
            withReplaceWhere
        }

      case _ =>
        withOptions
    }

    finalWriter.save(config.path)
  }
}