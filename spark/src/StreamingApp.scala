import org.apache.spark.sql.SparkSession

object StreamingApp {

    def run(appName: String) = {
        import org.apache.spark._
        import org.apache.spark.streaming._

        val conf = new SparkConf().setAppName(appName)
        val ssc = new StreamingContext(conf, Seconds(5))
        ssc.checkpoint("data/checkpoint") 
        val lines = ssc.socketTextStream("localhost", 9999)
        val words = lines.flatMap(_.split(" "))
        val pairs = words.map(word => (word, 1))
        val wordCounts = pairs.reduceByKey(_ + _)

        //wordCounts.print()
        val counts = wordCounts.countByWindow(Seconds(30), Seconds(5))
        counts.print() 
        
        ssc.start()             // Start the computation
        ssc.awaitTermination()  // Wait for the computation to terminate
    }

    def main(args: Array[String]) = {
        println("Streaming App")
        run(this.getClass().getSimpleName())
    }
}
