package service

import caliban.GraphQL
import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.schema.Annotations.{ GQLDeprecated, GQLDescription }
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers._

import zio._
import zio.stream._

import zio.query._

import scala.language.postfixOps

import caliban.schema.Schema
import com.sksamuel.elastic4s

case class RemoveIndexArgs(name: String)
case class LogSearchArgs(first: Int, size: Int, search: Option[String])
case class LogCountArgs(search: Option[String])

case class Queries(
  logsCount: LogCountArgs => ZIO[Any, Throwable,Long],
  logs: LogSearchArgs => ZIO[Any, Throwable,List[domain.Log]],
)
case class Mutations(removeIndex: RemoveIndexArgs => URIO[domain.Log, Boolean])
case class Subscriptions(calls: String => ZStream[domain.Log, Nothing, String])

object LogApi extends GenericSchema[domain.Log] {

  implicit val deleteIndexArgsSchema: Schema[Any, RemoveIndexArgs] = Schema.gen
  //implicit val deleteIndexArgsSchema = Schema.genMacro[RemoveIndexArgs].schema
  
  //implicit val mutationSchema = Schema.genMacro[Mutations].schema
  //implicit val mutationSchema = gen[Any, Mutations]

  implicit val logSearchArgsSchema: Schema[Any, LogSearchArgs] = Schema.gen
  implicit val logCountArgsSchema: Schema[Any, LogCountArgs] = Schema.gen
  implicit val esSearchSchema: Schema[Any, ES_Search] = Schema.gen
 
  implicit val queriesSchema: Schema[Any, Queries] =
    Schema.genMacro[Queries].schema

      /**
   * Returns a wrapper that prints slow queries
   * @param duration threshold above which queries are considered slow
   */

  def logSlowQueries(duration: Duration) =
    onSlowQueries(duration) { case (time, query) => {
      ZIO.logAnnotate(
        LogAnnotation("time_ms", time.toMillis.toString),
        LogAnnotation("query", query.replaceAll("(\\s|\\n)+", " ").trim())) {
          ZIO.logWarning(s"Slow query")
        }
      }
    }

  def api(queries: Queries) : GraphQL[Log] = 
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
      logSlowQueries(500 millis) @@ // wrapper that logs slow queries
      printErrors //@@                  // wrapper that logs errors
      //apolloTracing                   // wrapper for https://github.com/apollographql/apollo-tracing

}
