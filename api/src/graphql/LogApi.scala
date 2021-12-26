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
case class LogSearchArgs(first: Int, size: Int, search: Option[String])
case class LogCountArgs(search: Option[String])

case class Queries(
  logsCount: LogCountArgs => Task[Long],
  logs: LogSearchArgs => Task[List[Log]],
)
case class Mutations(removeIndex: RemoveIndexArgs => URIO[Has[LogService], Boolean])
case class Subscriptions(calls: String => ZStream[Has[LogService], Nothing, String])

object LogApi extends GenericSchema[Has[LogService]] {

  implicit val deleteIndexArgsSchema = gen[RemoveIndexArgs]
  implicit val mutationSchema = gen[Mutations]

  implicit val logSearchArgsSchema  = Schema.gen[LogSearchArgs]
  implicit val logCountArgsSchema  = Schema.gen[LogCountArgs]
  implicit val esSearchSchema  = Schema.gen[ES_Search]
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
