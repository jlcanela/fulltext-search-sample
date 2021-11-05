import zio._

object RunnerCli extends ZIOApp {

    type Environment = ZEnv with Has[Process]
    val tag = Tag[Environment]

    override def layer: ZLayer[Has[ZIOAppArgs],Any,Environment] = ZLayer.wire[Environment with Has[Process]](ZEnv.live, ProcessLive.layer)

    override def run: ZIO[Environment with ZEnv with Has[ZIOAppArgs],Any,Any] = for {
     (batchSuccess, indexSuccess, reportSuccess) <- Process.runBatch("https://github.com/jlcanela/spark-hands-on/raw/master/almhuette-raith.log/access.log.gz")
     _ <- Console.printLine(s"batchSuccess: $batchSuccess, indexSuccess: $indexSuccess, reportSuccess: $reportSuccess")
    } yield ()

}