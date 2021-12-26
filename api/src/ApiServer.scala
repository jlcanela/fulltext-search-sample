import zio._
import zio.stream._
import zhttp.http._
import zhttp.service.Server
import caliban.ZHttpAdapter
import java.nio.file.Paths

object ApiServer extends App {
  private val graphiql = Http.fromEffect(ZIO.succeed(
    Response.http(content =
      HttpData.fromStream(ZStream.fromResource("graphiql.html"))
    )
  ))
  private val data1 = Http.fromEffect(ZIO.succeed(
    Response.http(content =
      HttpData.fromStream(ZStream.fromFile(Paths.get("api", "resources", "data1.json")))
    )
  ))

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
    logService <- ZIO.service[LogService.LogService]
    logSearch = (args: LogSearchArgs) => logService.findLogs(args.first, args.size, args.search)
    logCount = (args: LogCountArgs) => logService.countLogs(args.search)
    queries <- ZIO.succeed(Queries(logCount, logSearch))
    interpreter <- LogApi.api(queries).interpreter
    _ <- Server
      .start(
        8088,
        CORS(
        Http.route {
          case _ -> Root / "api" / "graphql" =>
            ZHttpAdapter.makeHttpService(interpreter)
          case _ -> Root / "ws" / "graphql" =>
            ZHttpAdapter.makeWebSocketService(interpreter)
          case _ -> Root / "graphiql" => graphiql
          case _ -> Root / "data" => data1
          case _ -> Root / "app" / resource => app(resource)
        },
        config = CORSConfig(anyOrigin = true))
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
