import org.apache.spark.sql.SparkSession
object OlistCli {
  
    def listDelayedDeliveries(spark: SparkSession) = {
        val df = spark.read.option("header", true).csv("data/olist/file.csv")
        df.show
    }

    def run(f: SparkSession => Unit) = {
        val builder = SparkSession.builder.appName("Spark Batch")  
        val spark = builder.getOrCreate()
        f(spark)
        spark.close
    }

    def main(args: Array[String]) = {
        println("Olist Cli: XXX")
        run(listDelayedDeliveries _)
    }
}
