import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Dataset

import org.apache.spark.sql.Encoder
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.types.StructType

import org.apache.spark.sql.functions._
import org.apache.spark.sql.avro.functions._

case class Log(ip: String, ident: String, user: String, datetime: String, status: String, size: String, referer: String, userAgent: String, unk: String, method: String, uri: String, http: String)

object SparkStreaming {

  def run = {

    import org.apache.spark._
    import org.apache.spark.streaming._
    import org.apache.spark.streaming.StreamingContext._ // not necessary since Spark 1.3

    val pageViewAvro = """{
  "connect.name": "ksql.pageviews",
  "fields": [
    {
      "name": "viewtime",
      "type": "long"
    },
    {
      "name": "userid",
      "type": "string"
    },
    {
      "name": "pageid",
      "type": "string"
    }
  ],
  "name": "pageviews",
  "namespace": "ksql",
  "type": "record"
}"""
    val conf =
      new SparkConf().setMaster("local[2]").setAppName("KafkaClient")
    val sc = SparkContext.getOrCreate(conf)
    val spark = SparkSession.builder.getOrCreate()

    import spark.implicits._

    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      //.option("subscribe", "pageviews")
      .option("subscribe", "users")
      .option("startingOffsets", "latest")
      .load()
      .select(
        col("key").cast("string"),
        col("value"),
       // from_avro(col("value"), pageViewAvro).as("avro")
      )

    df.printSchema()

    val data = df
      .selectExpr("cast(value as string) as value")
     // .filter(col("userid").=!=(""))

    val query2 = data
    .writeStream
    .format("console")
    .start()
  
    query2.awaitTermination()
  
  }

}
