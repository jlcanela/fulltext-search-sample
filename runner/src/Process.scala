import zio._
import java.nio.file.Files
//import java.nio.path.Paths
import zio.internal.Blocking
import java.nio.file.Paths
import java.net.URI
import java.nio.file.Path

trait Process {
  def runBatch(path: String): ZIO[Has[Console], Throwable, (Boolean, Boolean, Boolean)]
}

object Process {
    def runBatch(path: String): ZIO[Has[Process] with Has[Console], Throwable, (Boolean, Boolean, Boolean)] = ZIO.serviceWith(_.runBatch(path))
}

case class ProcessLive(console: Console, clock: Clock) extends Process {
    def fetchFile(path: String) = for {
          uri     <- ZIO.succeed(new URI(path))
          file = uri.getPath.split("/").reverse.head
          _       <- console.printLine(file)
          exist   <- ZIO.attempt(Files.exists(Paths.get(file))).orDie
          process <- if (exist) console.printLine(s"$file already exists") else ZIO.attempt(os.proc("curl", "-o", file.toString, "-L", path).spawn().join(1000*30))
    } yield (file, !exist)

    def runSpark(command: String*) = {
      val params: Array[String] = Array("spark-submit", "--class", "SparkCli", s"${os.pwd}/out/spark/assembly/dest/out.jar") ++ command
      val cmd = os.Shellable.ArrayShellable(params)
      ZIO.attempt(os.proc(cmd).spawn(cwd=os.pwd).join())
    }
    //def runSpark(params: String*) = Console.printLine((List("spark-submit", "--class", "SparkCli", s"${os.pwd}/out/spark/assembly/dest/out.jar") ++ params).mkString(" "))

    override def runBatch(path: String): ZIO[Has[Console], Throwable, (Boolean, Boolean, Boolean)] = for {
      (file, fetched) <- fetchFile(path)
      batchSuccess <- runSpark("batch", "access.log.gz", "data/cleaned")
      indexSuccess <- runSpark("index", "data/cleaned")
      reportSuccess <- runSpark("report", "data/cleaned", "data/report")
    } yield (batchSuccess, indexSuccess, reportSuccess)
}

object ProcessLive {
    val layer: URLayer[Has[Console] with Has[Clock], Has[Process]] = (ProcessLive(_, _)).toLayer[Process]
}