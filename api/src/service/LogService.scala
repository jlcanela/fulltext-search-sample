import zio.stream.ZStream
import zio.{Has, Hub, Ref, UIO, URIO, ZLayer, ZIO}
import model.Log

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.SearchHit
import com.sksamuel.elastic4s.requests.searches.queries.{Query => ESQuery}
import zio.console
import zio.console.Console
import zio.stream.ZSink
import zio.query._
import com.sksamuel.elastic4s.RequestFailure

case class ElasticError(err: RequestFailure) extends Exception

case class LogResult(count: Long, logs: List[Log])

object LogService {

  trait LogService {
    def findLogs(from: Int, size: Int, search: Option[String]): ZIO[Any, Throwable, List[Log]]
    def countLogs(search: Option[String]): ZIO[Any, Throwable, Long]
    def removeIndex(name: String): UIO[Boolean]
    def calls: ZStream[Any, Nothing, String]
  }

  def findLogs(from: Int, size: Int, search: Option[String]): ZIO[Has[LogService], Throwable, List[Log]] =
    ZIO.serviceWith(_.findLogs(from, size, search))

  def countLogs(search: Option[String]): ZIO[Has[LogService], Throwable, Long] = 
    ZIO.serviceWith(_.countLogs(search))

  def removeIndex(name: String): ZIO[Has[LogService], Throwable, Boolean] =
    ZIO.serviceWith(_.removeIndex(name))

  def calls: ZStream[Has[LogService], Nothing, String] =
    ZStream.accessStream(_.get.calls)

  def make: ZLayer[Console with Has[
    ElasticService.ElasticService
  ], Nothing, Has[LogService]] = ZLayer.fromEffect(for {
    subscribers <- Hub.unbounded[String]
    console <- ZIO.service[Console.Service]
    elastic <- ZIO.service[ElasticService.ElasticService]
  } yield LogLive(console, subscribers, elastic))

}

case class LogLive(
    console: Console.Service,
    subscribers: Hub[String],
    elastic: ElasticService.ElasticService
) extends LogService.LogService {

  val x = ZStream
    .unwrapManaged(subscribers.subscribe.map(ZStream.fromQueue(_)))
    .run(ZSink.foreach(x => console.putStrLn(x)))

  def absolved[T](
      x: Either[RequestFailure, T]
  ): Either[Throwable, T] =
    x.fold(err => {
      println(err.toString())
    Left(ElasticError(err))}, x => Right(x))

  def matchAll = must(matchAllQuery())
  def searchText(txt: String) = query(txt)

  def searchOption(txt: Option[String]): ESQuery = txt.map(searchText).getOrElse(matchAll)

  def logs(
      from: Int,
      size: Int,
      txt: Option[String]
  ): ZIO[Any, Throwable, SearchResult] = ZIO.absolve(
    elastic
      .search {
        search("web")
          .query(searchOption(txt))//.filter(termQuery("uri", "php")))
          .from(from)
          .size(size)
          .sortByFieldAsc("datetime")
        // sourceInclude("gps", "populat*") sourceExclude("denonymn", "capit*")
      }.map(x => absolved(x))
    )  

  def findLogs(from: Int, size: Int, txt: Option[String]): ZIO[Any, Throwable, List[Log]] = (for {
    searchResult <- logs(from, size, txt)
    _ <- subscribers.publish("findLogs")
  } yield  searchResult.hits.toList
      .map(_.sourceAsMap)
      .map(Log.fromMap _)
  )

  def countLogs(txt: Option[String]): ZIO[Any,Throwable,Long] = ZIO.absolve(
    elastic.count(searchOption(txt)).map(x => absolved(x))
    )  
  def removeIndex(name: String) =
    subscribers.publish(s"removeIndex $name") 
    // elastic.removeIndex(name).orDie

  def calls: ZStream[Any, Nothing, String] =
    ZStream.unwrapManaged(subscribers.subscribe.map(ZStream.fromQueue(_)))
}
