import org.apache.spark.sql.SparkSession
import org.apache.log4j.Logger;

object Exec {
    val log = Logger.getLogger(Exec.getClass().getName())

    def run = {
       val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
       import spark.implicits._
       val logs = spark.read.text("access.log.gz")
       val count = logs.count
       log.info(s"count=$count")
       spark.close
       count
    }
}