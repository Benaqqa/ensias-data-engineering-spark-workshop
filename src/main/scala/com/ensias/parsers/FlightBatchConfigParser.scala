package com.ensias.parsers

import scopt.OParser

case class FlightBatchConfig(
  inputPath: String = "",
  outputPath: String = "",
  mode: String = "batch"
)

object FlightBatchConfigParser {

  private val builder = OParser.builder[FlightBatchConfig]

  private val parser = {
    import builder._
    OParser.sequence(
      programName("flight-batch"),
      opt[String]("input")
        .required()
        .action((x, c) => c.copy(inputPath = x))
        .text("Input flight dataset path"),
      opt[String]("output")
        .required()
        .action((x, c) => c.copy(outputPath = x))
        .text("Output dataset path"),
      opt[String]("mode")
        .optional()
        .action((x, c) => c.copy(mode = x))
        .text("Modes of processing, can either be batch (incremental) or init"),
    )
  }

  def parse(args: Array[String]): Option[FlightBatchConfig] =
    OParser.parse(parser, args, FlightBatchConfig())
}