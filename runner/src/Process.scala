import zio._
import java.nio.file.Files
//import java.nio.path.Paths
import zio.internal.Blocking
import java.nio.file.Paths
import java.net.URI
import java.nio.file.Path

trait Process {
  def runBatch(path: String): ZIO[Has[Console], Throwable, Unit]
}

object Process {
    def runBatch(path: String): ZIO[Has[Process] with Has[Console], Throwable, Unit] = ZIO.serviceWith(_.runBatch(path))
}

case class ProcessLive(console: Console, clock: Clock) extends Process {
    def fetchFile(path: String) = for {
          uri     <- ZIO.succeed(new URI(path))
          file = uri.getPath.split("/").reverse.head
          _       <- console.printLine(file)
          exist   <- ZIO.attempt(Files.exists(Paths.get(file))).orDie
          process <- if (exist) console.printLine(s"$file already exists") else ZIO.attempt(os.proc("curl", "-o", file.toString, "-L", path).spawn().join(1000*30))
    } yield (file, !exist)

    def runSpark(command: String) = ZIO.attempt(os.proc("spark-submit", "--class", "SparkCli", s"${os.pwd}/out/spark/assembly/dest/out.jar", command).spawn(cwd=os.pwd).join())

    override def runBatch(path: String): ZIO[Has[Console], Throwable, Unit] = for {
      (file, fetched) <- fetchFile(path)
      _ <- if (fetched) runSpark("batch") else Console.printLine("skipping 'spark run batch'")
      _ <- if (!fetched) runSpark("index") else Console.printLine("skipping 'spark run index'")
    } yield ()
}

object ProcessLive {
    val layer: URLayer[Has[Console] with Has[Clock], Has[Process]] = (ProcessLive(_, _)).toLayer[Process]
}