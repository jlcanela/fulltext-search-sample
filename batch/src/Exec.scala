import org.apache.spark.sql.functions._
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.log4j.Logger;

case class AccessLog(ip: String, ident: String, user: String, datetime: String, request: String, status: String, size: String, referer: String, userAgent: String, unk: String)
object AccessLog {
    val R = """^(?<ip>[0-9.]+) (?<identd>[^ ]) (?<user>[^ ]) \[(?<datetime>[^\]]+)\] \"(?<request>[^\"]*)\" (?<status>[^ ]*) (?<size>[^ ]*) \"(?<referer>[^\"]*)\" \"(?<useragent>[^\"]*)\" \"(?<unk>[^\"]*)\"""".r

    def fromString(s: String) = for {
        seq <- R.unapplySeq(s)
        Seq(ip: String, ident: String, user: String, datetime: String, request: String, status: String, size: String, referer: String, userAgent: String, unk: String) = seq
    } yield AccessLog(ip, ident, user, datetime, request, status, size, referer, userAgent, unk) 
}

object Exec {
    val log = Logger.getLogger(Exec.getClass().getName())
    
    def apply() = {
        val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
        run(spark)
        spark.close
    }

    def run(spark: SparkSession) = {
       val REQ_EX = "([^ ]+)[ ]+([^ ]+)[ ]+([^ ]+)".r
       
       import spark.implicits._

       def readSource = spark.read.text("access.log.gz").as[String]

       def cleanData(ds: Dataset[String]) = {
            val logs = ds.flatMap(AccessLog.fromString _)
            logs.printSchema()
            val dsWithTime = logs.withColumn("datetime", to_timestamp(logs("datetime"), "dd/MMM/yyyy:HH:mm:ss X"))
            val dsExtended = dsWithTime
                .withColumn("method", regexp_extract(dsWithTime("request"), REQ_EX.toString, 1))
                .withColumn("uri", regexp_extract(dsWithTime("request"), REQ_EX.toString, 2))
                .withColumn("http", regexp_extract(dsWithTime("request"), REQ_EX.toString, 3)).drop("request")
            dsExtended               
       }

       val dsExtended = cleanData(readSource)
       log.info(s"${dsExtended.schema.toDDL}")
       dsExtended.write.mode("Overwrite").json("out-json")
    }
}