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

object ApiServer extends ZIOAppDefault {

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
      Http.collectHttp[Request] {
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
    
  val fullLayer: ZLayer[Any, Nothing, ElasticService & LogService] = 
    ZLayer.make[ElasticService & LogService](
      ElasticService.live, 
      Elastic.live, 
      LogService.live,
      ZLayer.succeed(ElasticConfig.get)      
    )

    def run = for {
      args <- getArgs
      _    <- args match {
                case Chunk("start") => startServer.provide(fullLayer)
                case Chunk("clean-index") => cleanIndex.provide(fullLayer)
                case _ => for { 
                    _ <- Console.printLine(s"Command '${args.mkString(" ")}' not recognized").orDie
                  } yield ()
                }
    } yield ()

} 
