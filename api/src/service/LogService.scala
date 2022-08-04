import elastic._ 

import zio.{Hub, Console, UIO, ZIO}
import zio.stream._

import model.Log

import zio.ZLayer

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.SearchHit
import com.sksamuel.elastic4s.requests.searches.queries.{Query => ESQuery}
import com.sksamuel.elastic4s.RequestFailure

case class ElasticError(err: RequestFailure) extends Exception

case class LogResult(count: Long, logs: List[Log])

trait LogService {
  def findLogs(from: Int, size: Int, search: Option[String]): ZIO[Any, Throwable, List[Log]]
  def countLogs(search: Option[String]): ZIO[Any, Throwable, Long]
  def removeIndex(name: String): UIO[Boolean]
  // def calls: ZStream[Any, Nothing, String]
}

object LogService {

  def findLogs(from: Int, size: Int, search: Option[String]) =
    ZIO.serviceWithZIO[LogService](_.findLogs(from, size, search))

  def countLogs(search: Option[String]) = 
    ZIO.serviceWithZIO[LogService](_.countLogs(search))

  def removeIndex(name: String) =
    ZIO.serviceWithZIO[LogService](_.removeIndex(name))

//  def calls =  ZStream.accessStream(_.get.calls)

  val live = ZLayer.fromFunction(LogLive.apply _)

}

case class LogLive(
    elastic: ElasticService
) extends LogService {

  def matchAll = must(matchAllQuery())
  def searchText(txt: String) = query(txt)

  def searchOption(txt: Option[String]): ESQuery = txt.map(searchText).getOrElse(matchAll)

  def logs(
      from: Int,
      size: Int,
      txt: Option[String]
  ) =
    elastic
      .search {
        search("web")
          .query(searchOption(txt))//.filter(termQuery("uri", "php")))
          .from(from)
          .size(size)
          .sortByFieldAsc("datetime")
        // sourceInclude("gps", "populat*") sourceExclude("denonymn", "capit*")
      }

  def findLogs(from: Int, size: Int, txt: Option[String]) = for {
    searchResult <- logs(from, size, txt)
    //_ <- subscribers.publish("findLogs")
  } yield  searchResult.hits.toList
      .map(_.sourceAsMap)
      .map(Log.fromMap _)

  def countLogs(txt: Option[String]) = elastic.count(searchOption(txt))
  
  def removeIndex(name: String) = ZIO.unit.as(false)

}
