import zio._
import zio.ZEnvironment
object RunnerCli extends ZIOAppDefault {

    def run = job.provide(Process.live)

    def job = for {
     bir <- Process.runBatch("https://github.com/jlcanela/spark-hands-on/raw/master/almhuette-raith.log/access.log.gz")
     (batchSuccess, indexSuccess, reportSuccess) = bir
     _ <- Console.printLine(s"batchSuccess: $batchSuccess, indexSuccess: $indexSuccess, reportSuccess: $reportSuccess")
    } yield ()

}