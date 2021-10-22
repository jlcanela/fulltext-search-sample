import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Dataset

import org.apache.spark.sql.Encoder
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.types.StructType

case class Log(ip: String, ident: String, user: String, datetime: String, status: String, size: String, referer: String, userAgent: String, unk: String, method: String, uri: String, http: String)

object SparkStreaming {

  def run = {

    import org.apache.spark._
    import org.apache.spark.streaming._
    import org.apache.spark.streaming.StreamingContext._ // not necessary since Spark 1.3

    // Create a local StreamingContext with two working thread and batch interval of 1 second.
    // The master requires 2 cores to prevent a starvation scenario.

    val conf =
      new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val sc = SparkContext.getOrCreate(conf)
    val spark = SparkSession.builder.getOrCreate()
    val ssc = new StreamingContext(sc, Seconds(3))

    val logs = ssc.textFileStream("logs")/*.transform { rddRaw =>
      //val rawDfValue = rddRaw.selectExpr("CAST(value AS STRING)").as[String]
      val schema = ScalaReflection.schemaFor[Log].dataType.asInstanceOf[StructType]
      rddRaw.map
      val df = spark.createDataFrame(rddRaw)
      rddRaw.select(from_json(col("value"), schema).as("data")).select("data.*").rdd

    }
*/
    

  
    logs.print()

    /*
    // Create a DStream that will connect to hostname:port, like localhost:9999
    val lines = ssc.socketTextStream("localhost", 9999)

    // Split each line into words
    val words = lines.flatMap(_.split(" "))

    // Count each word in each batch
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)

    // Print the first ten elements of each RDD generated in this DStream to the console
    wordCounts.print()
*/
    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }

}
