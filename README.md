# 🚀 ENSIAS Data Engineering Workshop - Apache Spark
A hands-on data engineering workshop designed to introduce Apache Spark through real-world batch and streaming pipelines. This project provides a production-style environment built with Scala, Spark, and Delta Lake, running inside Docker for full reproducibility.

Participants will learn how to structure a modern data engineering project using a clean architecture approach, including:

- 📦 Batch processing pipelines
- 🌊 Streaming data pipelines
- 🧱 Modular design (models, parsers, transformations)
- 🗃️ Data processing with Spark DataFrames and Datasets
- 💾 Delta Lake for reliable data storage
- 🧪 Unit testing for data pipelines
- 🐳 Containerized development with Docker

The workshop emphasizes industry best practices, reproducibility, and scalable data engineering design patterns used in modern data platforms.

## Build Docker image

From the project root:

```bash
docker build -t ensias-spark-project .
```

This builds a Docker image containing:
- Java 8
- Scala / SBT environment
- Spark runtime
- Python 3.11
- Project dependencies

## Run the image (dev container)

To start an interactive development environment:

```bash
docker run -it --name ensias-spark-dev \
  -v $(pwd):/workspace \
  ensias-spark-project bash
```

Inside the container:
- /workspace is your project root
- Any code changes on host are reflected instantly

## Create new Spark Jobs 
### Batch Processing

Batch jobs follow this structure:

```yaml
com.ensias
 └── batch
 └── models
 └── parsers
 └── transformations
 └── utils
```
#### => models/
Define input/output schemas (case classes):
- Input case class (ex: FlightInputDataModel)
- Output case class (ex: FlightOutputDataModel)

#### => parsers/

Responsible for defining the parameters needed for your processing like input/output paths or any other param that may influence your transformation logic:

We first create the params case class
```scala
case class FlightBatchConfig(
  inputPath: String = "",
  outputPath: String = "",
  mode: String = "batch"
)
```

Then we define the parser in which we provide some details (name, description, required or optional ...) about each param. Checkout this in [FlightBatchConfigParser](src/main/scala/com/ensias/parsers/FlightBatchConfigParser.scala#L11-L36)

#### => transformations/

Business logic layer (pure transformations):
```scala
object FlightTransformations {
  def cleanFlights(df: DataFrame): DataFrame = {
    df.filter("price > 0")
  }
}
```
Create as many methods inside the class as needed to make code readable or to have reusable blocks in case repeated use of such transformation in the same processing

#### => batch/

Orchestration layer (entry point):
```scala
object FlightBatchJob {
  def run(spark: SparkSession, inputPath: String, outputPath: String): Unit = {
    val raw = spark.read.parquet(inputPath)
    val cleaned = FlightTransformations.cleanFlights(raw)
    cleaned.write.mode("overwrite").parquet(outputPath)
  }
}
```
### Stream Processing

Streaming jobs use the same structure but replace batch with:
```yaml
com.ensias
 └── streaming
 └── models
 └── parsers
 └── transformations
 └── utils
```

#### Streaming job example
```scala
object FlightStreamingJob {
  def run(spark: SparkSession): Unit = {
    val streamDF =
      spark.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "kafka:9092")
        .option("subscribe", "flights")
        .load()

    val processed =
      FlightTransformations.cleanFlights(streamDF)

    processed.writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
  }
}
```
## Run all tests (inside Docker)
```bash
sbt test
```
## Run a specific test

By class:

```bash
sbt "testOnly com.ensias.batch.FlightExampleBatchSpec"
```
By name filter:

```bash
sbt "testOnly com.ensias.batch.FlightExampleBatchSpec -- -z 2026-04-30"
```

## Build JAR inside Docker
```bash
sbt assembly
```
Output:
```
target/scala-2.12/ensias-de-workshop-spark-1.0.0-assembly.jar
```
## Run Batch job using spark-submit
```bash
spark-submit \
  --class com.ensias.batch.FlightExampleBatch \
  --master local[*] \
  --packages io.delta:delta-spark_2.12:3.2.0 \
  target/scala-2.12/ensias-de-workshop-spark-1.0.0-assembly.jar \
  --input src/test/resources/inputs/flightExampleData/year=2026/month=04/day=30 \
  --output target/test-output/apr30
```
## Run Streaming job using spark-submit
```bash
spark-submit \
  --class com.ensias.streaming.FlightStreamingJob \
  --master local[*] \
  --packages io.delta:delta-spark_2.12:3.2.0 \
  target/scala-2.12/ensias-de-workshop-spark-1.0.0-assembly.jar \
```
## Build, run, and test workflow (ENSIAS standard)
```bash
docker build -t ensias-spark-project .

docker run -it -v $(pwd):/workspace ensias-spark-project bash

sbt clean compile
sbt test
sbt package

spark-submit ...
```