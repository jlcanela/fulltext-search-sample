import caliban.GraphQL
import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.schema.Annotations.{ GQLDeprecated, GQLDescription }
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers._

import zio._
import zio.{Has, URIO, ZIO}
import zio.clock.Clock
import zio.console.Console
import zio.duration._
import zio.stream.ZStream

import zio.query._

import scala.language.postfixOps

import LogService.LogService
import ElasticService.ElasticService
import model.Log
import caliban.schema.Schema

case class RemoveIndexArgs(name: String)
case class LogArgs(first: Int, size: Int)

case class Queries(
  logsCount: Task[Long],
  logs: LogArgs => Task[List[Log]],
)
case class Mutations(removeIndex: RemoveIndexArgs => URIO[Has[LogService], Boolean])
case class Subscriptions(calls: String => ZStream[Has[LogService], Nothing, String])

object LogApi extends GenericSchema[Has[LogService]] {

  implicit val deleteIndexArgsSchema = gen[RemoveIndexArgs]
  implicit val mutationSchema = gen[Mutations]

  implicit val logArgsSchema  = Schema.gen[LogArgs]
  implicit val esSearchSchema  = Schema.gen[ES_Search]
  
  //implicit val characterSchema = Schema.gen[Any, Character]

  def getLogsCount(args: LogArgs): ZIO[Has[LogService.LogService], Throwable, Int] = for {
    logs <- LogService.findLogs(args.first, args.size)
  } yield logs.size

  def dummyLogsCount(args: LogArgs) = ZIO.succeed(1000000)
  def genLogs(args: LogArgs): List[Log] = List.tabulate(args.size)(x => Log.fromInt(x+args.first))
  def dummyLogs(args: LogArgs) = ZIO.succeed(genLogs(args))

  def getLogs(args: LogArgs): ZIO[Has[LogService.LogService], Throwable, List[Log]] = for {
    logs <- LogService.findLogs(args.first, args.size)
  } yield logs

  implicit val queriesSchema: Schema[Any, Queries] = Schema.gen[Queries]

  def api(queries: Queries) : GraphQL[Console with Clock with Has[LogService]] = 
    graphQL(
      RootResolver(
        queries,
        //Mutations(args => LogService.removeIndex(args.name).orDie)
       // Subscriptions(String => LogService.calls)
      )
    ) @@
      maxFields(200) @@               // query analyzer that limit query fields
      maxDepth(30) @@                 // query analyzer that limit query depth
      timeout(3 seconds) @@           // wrapper that fails slow queries
      printSlowQueries(500 millis) @@ // wrapper that logs slow queries
      printErrors //@@                  // wrapper that logs errors
      //apolloTracing                   // wrapper for https://github.com/apollographql/apollo-tracing

}
