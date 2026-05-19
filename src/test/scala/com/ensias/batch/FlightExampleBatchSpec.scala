package com.ensias.batch

import io.delta.sql.DeltaSparkSessionExtension
import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.Paths

class FlightExampleBatchTest extends AnyFunSuite {

  // ---------------------------------------------------------------------------
  // DELTA-ENABLED SPARK SESSION
  // ---------------------------------------------------------------------------

  lazy val spark: SparkSession =
    SparkSession.builder()
      .appName("FlightExampleBatchTest")
      .master("local[*]")
      .config(
        "spark.sql.extensions",
        classOf[DeltaSparkSessionExtension].getName
      )
      .config(
        "spark.sql.catalog.spark_catalog",
        "org.apache.spark.sql.delta.catalog.DeltaCatalog"
      )
      .config("spark.sql.shuffle.partitions", "1")
      .config("spark.ui.enabled", "false")
      .getOrCreate()

  // ---------------------------------------------------------------------------
  // Helper to run batch
  // ---------------------------------------------------------------------------

  def runBatch(input: String, output: String): Unit = {
    FlightExampleBatch.main(Array(
      "--input", input,
      "--output", output
    ))
  }

  // ---------------------------------------------------------------------------
  // TEST 1
  // ---------------------------------------------------------------------------

  test("FlightExampleBatch should process 2026-04-30 flights into Delta") {

    val inputPath = "src/test/resources/inputs/flightExampleData/year=2026/month=04/day=30"

    val outputPath = "target/test-output/apr30"

    runBatch(inputPath, outputPath)

    val result =
      spark.read.format("delta").load(outputPath)

    assert(result.count() > 0)
    assert(result.columns.contains("flightId"))
  }

  // ---------------------------------------------------------------------------
  // TEST 2
  // ---------------------------------------------------------------------------

  test("FlightExampleBatch should process 2026-05-19 flights into Delta") {

    val inputPath = "src/test/resources/inputs/flightExampleData/year=2026/month=05/day=19"

    val outputPath = "target/test-output/may19"

    runBatch(inputPath, outputPath)

    val result =
      spark.read.format("delta").load(outputPath)

    assert(result.count() > 0)
    assert(result.columns.contains("carrierName"))
  }
}