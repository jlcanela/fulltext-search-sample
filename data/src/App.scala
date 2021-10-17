import zio._

object App extends ZIOApp {

    type Environment = ZEnv with Has[Process]
    val tag = Tag[Environment]
//  override implicit def tag: Tag[Environment] = Tag???

    override def layer: ZLayer[Has[ZIOAppArgs],Any,Environment] = ZLayer.wire[Environment with Has[Process]](ZEnv.live, ProcessLive.layer)

    override def run: ZIO[Environment with ZEnv with Has[ZIOAppArgs],Any,Any] = for {
     _ <- Process.download("https://github.com/jlcanela/spark-hands-on/raw/master/almhuette-raith.log/access.log.gz")
    } yield ()

}