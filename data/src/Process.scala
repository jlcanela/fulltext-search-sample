import zio._
import java.nio.file.Files
//import java.nio.path.Paths
import zio.internal.Blocking
import java.nio.file.Paths
import java.net.URI
import java.nio.file.Path

trait Process {
  def download(path: String): ZIO[Has[Console], Throwable, Unit]
}

object Process {

    def download(path: String): ZIO[Has[Process] with Has[Console], Throwable, Unit] = ZIO.serviceWith(_.download(path))

}

case class ProcessLive(console: Console, clock: Clock) extends Process {
def fetchFile(path: String) = for {
      uri     <- ZIO.succeed(new URI(path))
      file = uri.getPath.split("/").reverse.head
      _       <- console.printLine(file)
      exist   <- ZIO.attempt(Files.exists(Paths.get(file))).orDie
      process <- if (exist) console.printLine(s"$file already exists") else ZIO.attempt(os.proc("curl", "-o", file.toString, "-L", path).spawn().join(1000*30))
} yield file

    override def download(path: String): ZIO[Has[Console], Throwable, Unit] = for {
      file <- fetchFile(path)
      _ <- console.printLine(List("spark-submit", "--class", "App", s"${os.pwd}/out/batch/assembly/dest/out.jar").mkString(" "))
      //_ <- ZIO.attempt(os.proc("spark-submit", "--class", "App", s"${os.pwd}/out/batch/assembly/dest/out.jar", "batch").spawn(cwd=os.pwd).join())
      _ <- ZIO.attempt(os.proc("spark-submit", "--class", "App", s"${os.pwd}/out/batch/assembly/dest/out.jar", "index").spawn(cwd=os.pwd).join())
    } yield ()
}

object ProcessLive {
    val layer: URLayer[Has[Console] with Has[Clock], Has[Process]] = (ProcessLive(_, _)).toLayer[Process]
}