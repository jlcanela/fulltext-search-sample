import zio._

object App extends ZIOApp {

    import org.apache.log4j.Logger

    val log = Logger.getLogger(App.getClass().getName())

    type Environment = ZEnv

    val tag = Tag[Environment]

    override def layer: ZLayer[Has[ZIOAppArgs],Any,Environment] = ZLayer.wire[Environment](ZEnv.live)

    def run(command: String) = command match {
        case "batch" => Exec.clean _
        case "index" => Exec.index _
    }
    override def run: ZIO[Environment with ZEnv with Has[ZIOAppArgs],Any,Any] = for {
     args <- getArgs if args.length > 0
     _ <- ZIO.attempt(Exec(run(args(0))))
     _ <- ZIO.attempt(log.info(s"finished"))
    } yield ()

}
