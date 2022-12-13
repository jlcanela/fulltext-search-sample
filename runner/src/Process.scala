import zio._
import zio.internal.Blocking
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.net.URI

trait Process {
  def runBatch(path: String): ZIO[Any, Throwable, (Boolean, Boolean, Boolean)]
}

object Process {
  def runBatch(path: String): ZIO[Process, Throwable, (Boolean, Boolean, Boolean)] = ZIO.serviceWithZIO[Process](_.runBatch(path))

  val live: ZLayer[Any, Nothing, Process] = ZLayer.fromFunction(ProcessLive.apply _)
    
}

case class ProcessLive(/*console: Console, clock: Clock*/) extends Process {
    def fetchFile(path: String) = for {
          uri     <- ZIO.succeed(new URI(path))
          file = uri.getPath.split("/").reverse.head
          _       <- Console.printLine(file)
          exist   <- ZIO.attempt(Files.exists(Paths.get(file))).orDie
          process <- if (exist) Console.printLine(s"$file already exists") else ZIO.attempt(os.proc("curl", "-o", file.toString, "-L", path).spawn().join(1000*30))
    } yield (file, !exist)

    def runSpark(command: String*) = {
      val params: Array[String] = Array("spark-submit", "--class", "SparkCli", s"${os.pwd}/out/spark/assembly/dest/out.jar") ++ command
      val cmd = os.Shellable.ArrayShellable(params)
      ZIO.attempt(os.proc(cmd).spawn(cwd=os.pwd).join())
    }

    override def runBatch(path: String) = for { //: ZIO[Any, Throwable, (Boolean, Boolean, Boolean)] = for {
      ff <- fetchFile(path)
      (file, fetched) = ff
      batchSuccess <- runSpark("batch", "access.log.gz", "data/cleaned")
      indexSuccess <- runSpark("index", "data/cleaned")
      reportSuccess <- runSpark("report", "data/cleaned", "data/report")
    } yield (batchSuccess, indexSuccess, reportSuccess)
}
