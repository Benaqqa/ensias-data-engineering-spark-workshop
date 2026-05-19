package com.ensias.batch

import com.ensias.SparkApp
import com.ensias.models._
import com.ensias.parsers.FlightBatchConfigParser
import com.ensias.transformations.FlightLogicProcessing
import com.ensias.utils.SparkIOUtils
import org.apache.spark.sql.SparkSession

object FlightExampleBatch extends SparkApp {

  override def appName: String = "example-flight-batch-job"

  override def run(args: Array[String]): Unit = {
    import spark.implicits._

    // -----------------------------------------------------------------------
    // 1. Parse CLI config
    // -----------------------------------------------------------------------

    val config = FlightBatchConfigParser.parse(args) match {
      case Some(cfg) => cfg
      case None =>
        logger.error("Invalid arguments provided to FlightExampleBatch")
        sys.exit(1)
    }

    logger.info(s"Input path  : ${config.inputPath}")
    logger.info(s"Output path : ${config.outputPath}")

    // -----------------------------------------------------------------------
    // 2. READ JSON using SparkIOUtils
    // -----------------------------------------------------------------------

    val inputDf = SparkIOUtils.readDataFrame(
        spark,
        SparkIOUtils.ReadConfig(
          "json",
          config.inputPath,
          Map(
            "multiline" -> "true"
          )
        )
      )

    logger.info(s"Loaded flights: ${inputDf.count()}")

    // -----------------------------------------------------------------------
    // 3. TRANSFORM
    // -----------------------------------------------------------------------

    val outputDS = FlightLogicProcessing.process(inputDf.as[FlightInputDataModel])

    // -----------------------------------------------------------------------
    // 4. WRITE TO DELTA (partitioned by carrierName)
    // -----------------------------------------------------------------------

    logger.info(s"Writing into ${outputDS.count} records into path : ${config.outputPath}")
    SparkIOUtils.writeDataFrame(
      outputDS.toDF(),
      SparkIOUtils.WriteConfig(
        "delta",
        config.outputPath,
        "overwrite",
        // Partitioning strategy
        Seq("carrierName")
      )
    )

    logger.info(s"Delta output written successfully to ${config.outputPath}")
  }
}