import zio.stream.ZStream
import zio.{ Has, Hub, Ref, UIO, URIO, ZLayer, ZIO }
import model.Log

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.SearchHit
import zio.console

case class LogResult(count: Long, logs: List[Log])
//  implicit val charactersArgsSchema = gen[CharactersArgs]

object LogService {

  trait LogService {
    def findLogs(from: Int, size: Int): UIO[LogResult]
    def removeIndex(name: String): UIO[Boolean]
    def calls: ZStream[Any, Nothing, String]
  }

  def findLogs(from: Int, size: Int): URIO[Has[LogService], LogResult] =
    ZIO.serviceWith(_.findLogs(from, size))

  def removeIndex(name: String): ZIO[Has[LogService], Throwable, Boolean] = ZIO.serviceWith(_.removeIndex(name))
  
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

    //def deleteIndex(name: String): UIO[Unit] = ZIO.succeed(())
    def removeIndex(name: String) = elastic.removeIndex(name).orDie

    def calls: ZStream[Any, Nothing, String] =
        ZStream.unwrapManaged(subscribers.subscribe.map(ZStream.fromQueue(_)))
}
