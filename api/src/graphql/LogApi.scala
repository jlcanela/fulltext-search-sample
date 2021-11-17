import caliban.GraphQL
import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.schema.Annotations.{ GQLDeprecated, GQLDescription }
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers._

import zio.{Has, URIO, ZIO}
import zio.clock.Clock
import zio.console.Console
import zio.duration._
import zio.stream.ZStream

import scala.language.postfixOps

import LogService.LogService
import ElasticService.ElasticService

case class RemoveIndexArgs(name: String)
case class LogArgs(first: Int, size: Int)

object LogApi extends GenericSchema[Has[LogService]] {

  case class Queries(logs: LogArgs => URIO[Has[LogService], LogResult])
  case class Mutations(removeIndex: RemoveIndexArgs => URIO[Has[LogService], Boolean])
  case class Subscriptions(calls: String => ZStream[Has[LogService], Nothing, String])

  implicit val deleteIndexArgsSchema = gen[RemoveIndexArgs]
  implicit val mutationSchema = gen[Mutations]

  val api: GraphQL[Console with Clock with Has[LogService]] =
    graphQL(
      RootResolver(
        Queries(
          args => LogService.findLogs(args.first, args.size),
        ),
        Mutations(args => LogService.removeIndex(args.name).orDie),
        Subscriptions(String => LogService.calls)
      )
    ) @@
      maxFields(200) @@               // query analyzer that limit query fields
      maxDepth(30) @@                 // query analyzer that limit query depth
      timeout(3 seconds) @@           // wrapper that fails slow queries
      printSlowQueries(500 millis) @@ // wrapper that logs slow queries
      printErrors //@@                  // wrapper that logs errors
      //apolloTracing                   // wrapper for https://github.com/apollographql/apollo-tracing

}
