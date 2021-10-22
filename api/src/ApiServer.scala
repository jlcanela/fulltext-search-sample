import zio._
import zio.stream._
import zhttp.http._
import zhttp.service.Server
import caliban.ZHttpAdapter

object ApiServer extends App {
  private val graphiql = Http.succeed(
    Response.http(content =
      HttpData.fromStream(ZStream.fromResource("graphiql.html"))
    )
  )

  private def app(resource: String) = Http.succeed(
    Response.http(content =
      HttpData.fromStream(ZStream.fromResource(s"app/$resource"))
    )
  )

  def layer
      : ZLayer[Any, Nothing, ZEnv with Has[
        ElasticService.ElasticService
      ] with Has[LogService.LogService]] =
    (ZEnv.live >+> ElasticService.make >+> LogService.make).orDie

  def startServer = for {
    interpreter <- LogApi.api.interpreter
    _ <- Server
      .start(
        8088,
        Http.route {
          case _ -> Root / "api" / "graphql" =>
            ZHttpAdapter.makeHttpService(interpreter)
          case _ -> Root / "ws" / "graphql" =>
            ZHttpAdapter.makeWebSocketService(interpreter)
          case _ -> Root / "graphiql" => graphiql
          case _ -> Root / "app" / resource => app(resource)
        }
      ).forever
  } yield ()

  def cleanIndex: ZIO[Has[ElasticService.ElasticService], Throwable, ExitCode] =  for {
    _ <- ElasticService.removeIndex("logs")
  } yield ExitCode(0)
  /*)
    .provideLayer(layer)
    .exitCode
  */
  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    (args match {
      case List("start") => startServer
      case List("clean-index") => cleanIndex.provideLayer(layer).exitCode
      case _ => for { 
          _ <- console.putStrLn(s"Command '${args.mkString(" ")}' not recognized").orDie
      } yield ExitCode(-1)
    }).provideLayer(layer)
    .exitCode
}
