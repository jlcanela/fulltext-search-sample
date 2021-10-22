import example.ExampleData._
import zio.stream.ZStream
import zio.{ Has, Hub, Ref, UIO, URIO, ZLayer, ZIO }
import model.Log

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.SearchHit
import zio.console

case class LogResult(count: Long, logs: List[Log])

object LogService {

  trait LogService {
    def findLogs(from: Int, size: Int): UIO[LogResult]
    def deleteIndex(name: String): UIO[Unit]
    def calls: ZStream[Any, Nothing, String]
  }

  def findLogs(from: Int, size: Int): URIO[Has[LogService], LogResult] =
    ZIO.serviceWith(_.findLogs(from, size))

  def deleteIndex(name: String): URIO[Has[LogService], Unit] = ZIO.serviceWith(_.deleteIndex(name))
  
  def calls: ZStream[Has[LogService], Nothing, String] =
    ZStream.accessStream(_.get.calls)

  def make: ZLayer[Has[ElasticService.ElasticService], Nothing, Has[LogService]] = ZLayer.fromEffect(for {
    subscribers <- Hub.unbounded[String]
    elastic <- ZIO.service[ElasticService.ElasticService]
  } yield LogLive(subscribers, elastic))
  
}

case class LogLive(subscribers: Hub[String], elastic: ElasticService.ElasticService) extends LogService.LogService {

    def findLogs(from: Int, size: Int): UIO[LogResult] = (for {
        searchResult <- ZIO.absolve(elastic.search {
            search("web").query(must(matchAllQuery()).filter(termQuery("uri", "php"))).from(from).size(size).sortByFieldAsc("datetime")
            // sourceInclude("gps", "populat*") sourceExclude("denonymn", "capit*")
        })
        _ <- subscribers.publish("findLogs")
    } yield LogResult(searchResult.count, searchResult.hits
      .toList.map(_.sourceAsMap)
      .map(Log.fromMap _)))
      .fold(
        x => LogResult(0, List(Log("","","","","","","","","","", "", x.toString))), 
        x => x)

    def deleteIndex(name: String): UIO[Unit] = ZIO.succeed(())

    def calls: ZStream[Any, Nothing, String] =
        ZStream.unwrapManaged(subscribers.subscribe.map(ZStream.fromQueue(_)))
}
