import zio._

object App extends ZIOApp {

    import org.apache.log4j.Logger

    val log = Logger.getLogger(App.getClass().getName())

    type Environment = ZEnv

    val tag = Tag[Environment]

    override def layer: ZLayer[Has[ZIOAppArgs],Any,Environment] = ZLayer.wire[Environment](ZEnv.live)

    override def run: ZIO[Environment with ZEnv with Has[ZIOAppArgs],Any,Any] = for {
     _ <- ZIO.attempt(Exec())
     _ <- ZIO.attempt(log.info(s"finished"))
    } yield ()

}
