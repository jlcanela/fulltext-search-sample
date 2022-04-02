import zio._
import zio.stream._
import zhttp.http._
import zhttp.service.Server
import caliban.ZHttpAdapter
import java.nio.file.Paths
import com.sksamuel.elastic4s.HttpResponse
import caliban.GraphQLInterpreter
import caliban.CalibanError

import elastic._ 

object ApiServer extends App {

  private val graphiql = Http.fromZIO(ZIO.succeed(
    Response.http(data =
      HttpData.fromStream(ZStream.fromResource("graphiql.html"))
    )
  ))

  private val data1 = Http.fromZIO(ZIO.succeed(
    Response.http(data =
      HttpData.fromStream(ZStream.fromFile(Paths.get("api", "resources", "data1.json").toFile))
    )
  ))

 private def static(resource: String) = Http.succeed(
    Response.http(data =
      HttpData.fromStream(ZStream.fromResource(s"app/$resource"))
    )
  )

  def startServer = for {
    logService <- ZIO.service[LogService]
    logSearch = (args: LogSearchArgs) => logService.findLogs(args.first, args.size, args.search)
    logCount = (args: LogCountArgs) => logService.countLogs(args.search)
    queries <- ZIO.succeed(Queries(logCount, logSearch))
    interpreter <- LogApi.api(queries).interpreter
    _ <- Server
      .start(
        8088,
        Http.route[Request] {
          case _ -> !! / "api" / "graphql" =>  ZHttpAdapter.makeHttpService(interpreter)
          // case _ -> !! / "ws" / "graphql" => ZHttpAdapter.makeWebSocketService(interpreter)
          case _ -> !! / "app" / resource => static(resource)
          // case _ -> !! / "data" => data1
      }
      ).forever
  } yield ()

  def cleanIndex: ZIO[ElasticService, Throwable, ExitCode] =  for {
    _ <- ElasticService.removeIndex("logs")
  } yield ExitCode(0)

  override def run(args: List[String]) =
    (args match {
      case List("start") => startServer
      case List("clean-index") => cleanIndex//.provide(ElasticService.live)
      case _ => for { 
          _ <- Console.printLine(s"Command '${args.mkString(" ")}' not recognized").orDie
      } yield ()
    }).exitCode.provide(
      Console.live,
      Clock.live,
      LogService.live, 
      Elastic.live,
      ElasticService.live,
      ZLayer.succeed(ElasticConfig.get))
}
