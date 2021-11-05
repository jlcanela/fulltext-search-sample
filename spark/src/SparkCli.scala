import zio._
import org.apache.spark.sql.SparkSession

object SparkCli extends ZIOApp {

    import org.apache.log4j.Logger

    type Environment = ZEnv

    val tag = Tag[Environment]

    override def layer: ZLayer[Has[ZIOAppArgs],Any,Environment] = ZLayer.wire[Environment](ZEnv.live)

    def run(command: Array[String]) = command match {
        case Array("batch", in, out) => SparkBatch.run((spark: SparkSession) => SparkBatch.clean(spark, in, out))
        case Array("quill") => SparkBatch.run(SparkBatch.cleanQuill _)
        case Array("index", path) => SparkBatch.run((spark: SparkSession) => SparkBatch.index(spark, path))
        case Array("report", in, out) => SparkBatch.run((spark: SparkSession) => SparkBatch.report(spark, in, out))
        case Array("stream") => SparkStreaming.run
        case _ => println(s"command '$command' not recognized (batch|index)")
    }
    override def run: ZIO[Environment with ZEnv with Has[ZIOAppArgs],Any,Any] = for {
     args <- getArgs if args.length > 0
     _ <- ZIO.attempt(run(args.toArray))
     _ <- Console.printLine(s"finished")
    } yield ()

}
