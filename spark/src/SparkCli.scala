import zio._

object SparkCli extends ZIOApp {

    import org.apache.log4j.Logger

    type Environment = ZEnv

    val tag = Tag[Environment]

    override def layer: ZLayer[Has[ZIOAppArgs],Any,Environment] = ZLayer.wire[Environment](ZEnv.live)

    def run(command: String) = command match {
        case "batch" => SparkBatch.run(SparkBatch.clean _)
        case "index" => SparkBatch.run(SparkBatch.index _)
        case "report" => SparkBatch.run(SparkBatch.report _)
        case "streaming" => SparkStreaming.run
        case _ => println(s"command '$command' not recognized (batch|index)")
    }
    override def run: ZIO[Environment with ZEnv with Has[ZIOAppArgs],Any,Any] = for {
     args <- getArgs if args.length > 0
     _ <- ZIO.attempt(run(args(0)))
     _ <- Console.printLine(s"finished")
    } yield ()

}
