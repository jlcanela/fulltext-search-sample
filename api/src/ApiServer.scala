import example.ExampleData._
import example.{ ExampleApi, ExampleService }

import zio._
import zio.stream._
import zhttp.http._
import zhttp.service.Server
import caliban.ZHttpAdapter

object ApiServer extends App {
  private val graphiql = Http.succeed(Response.http(content = HttpData.fromStream(ZStream.fromResource("graphiql.html"))))

  def layer: ZLayer[Any, Nothing, ZEnv with ExampleService.ExampleService with Has[ElasticService.ElasticService] with Has[LogService.LogService]] =
     (ZEnv.live >+> ExampleService.make(sampleCharacters) >+> ElasticService.make >+> LogService.make).orDie
  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    (for {
      interpreter <- LogApi.api.interpreter
      _           <- Server
                       .start(
                         8088,
                         Http.route {
                           case _ -> Root / "api" / "graphql" => ZHttpAdapter.makeHttpService(interpreter)
                           case _ -> Root / "ws" / "graphql"  => ZHttpAdapter.makeWebSocketService(interpreter)
                           case _ -> Root / "graphiql"        => graphiql
                         }
                       )
                       .forever
    } yield ())
      .provideLayer(layer)
      .exitCode
}
