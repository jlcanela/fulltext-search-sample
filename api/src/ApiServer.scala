import zio._

/*object ApiServer extends App {

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    val logger = LoggerFactory.getLogger(AppServer.getClass);
    // type Environment = ZEnv

    // val tag = Tag[Environment]

    // override def layer: ZLayer[Has[ZIOAppArgs],Any,Environment] = ZLayer.wire[Environment](ZEnv.live)

    override def run: ZIO[Environment with ZEnv with Has[ZIOAppArgs],Any,Any] = for {
        _ <- ZIO.succeed(logger.info("start api"))
     hits <- Api.searchApi
     _ <- Console.printLine(hits.map(_.sourceAsMap.mkString("|")).mkString("\n"))
    
    } yield ()

}
*/