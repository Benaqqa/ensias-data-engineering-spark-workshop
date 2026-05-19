# 🧠 Spark Architecture (Quick Overview)

Apache Spark follows a distributed master–worker architecture designed to process large-scale data in parallel.

## 🏗️ High-Level Architecture
```
            +----------------------+
            |   Driver Program     |
            | (SparkSession)       |
            +----------+-----------+
                       |
                       | DAG / Job Plan
                       v
            +----------------------+
            | Cluster Manager      |
            | (YARN / K8s / etc.)  |
            +----------+-----------+
                       |
        ---------------------------------
        |               |               |
        v               v               v
+--------------+ +--------------+ +--------------+
|  Executor 1  | |  Executor 2  | |  Executor 3  |
|  Tasks       | |  Tasks       | |  Tasks       |
+--------------+ +--------------+ +--------------+
```
## ⚙️ Core Components
### 🧑‍💻 Driver
Entry point of the application
Builds the execution plan (DAG)
Schedules tasks
### 🚀 Executors
Run tasks in parallel
Store intermediate data in memory/disk
Return results to driver
### 📦 Cluster Manager
Allocates resources
Manages executors
Examples: YARN, Kubernetes, Spark Standalone
### 🔄 Execution Flow
```
Code → DAG → Stages → Tasks → Executors → Result
```
### ⚡ Key Idea
- 🔹 Transformations are lazy
- 🔹 Nothing executes until an action is triggered (write, show, count)
- 🔹 Spark builds an optimized execution plan (DAG)

## Commun syntaxe and transformations
This section summarizes the most frequently used DataFrame operations in Spark for batch and streaming pipelines.

### 📥 Creating a DataFrame

```scala
spark.read.format("parquet").load(path)
spark.read.csv(path)
spark.read.json(path)
```

### 🔍 Selecting Columns
```scala
df.select("col1", "col2")
df.selectExpr("col1", "col2 + 1 as col3")
```

### 🎯 Filtering Data
```scala
df.filter("age > 18")
df.where(col("age") > 18)
```

### 🧱 Adding / Modifying Columns

```scala
import org.apache.spark.sql.functions._

df.withColumn("newCol", col("existingCol") * 2)
df.withColumnRenamed("oldName", "newName")
```

### 🔄 Aggregations

```scala
df.groupBy("category")
  .count()

df.groupBy("category")
  .agg(
    avg("price"),
    max("price")
  )
```

### 🔗 Joins

```scala
df1.join(df2, Seq("id"), "inner")
df1.join(df2, df1("id") === df2("id"), "left")
```

📊 Sorting

```scala
df.orderBy(desc("price"))
df.sort("name")
```

🧹 Cleaning Data

```scala
df.na.drop()
df.na.fill(0)
df.dropDuplicates()
```

### 🔁 Common Transformations Pattern
```
Raw Data
   ↓
filter()
   ↓
select()
   ↓
withColumn()
   ↓
groupBy()
   ↓
write()
```
## 💡 In a nuttshell
Spark enables fast, scalable, distributed computing across batch and streaming workloads — the foundation of this ENSIAS workshop 🚀