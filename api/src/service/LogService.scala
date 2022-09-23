package service

import zio.{Hub, Console, UIO, ZIO}
import zio.stream._

//import domain.Log

import zio.ZLayer

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.SearchHit
import com.sksamuel.elastic4s.requests.searches.queries.{Query => ESQuery}
import com.sksamuel.elastic4s.RequestFailure

case class ElasticError(err: RequestFailure) extends Exception

case class LogResult(count: Long, logs: List[domain.Log])

trait Log {
  def findLogs(from: Int, size: Int, search: Option[String]): ZIO[Any, Throwable, List[domain.Log]]
  def countLogs(search: Option[String]): ZIO[Any, Throwable, Long]
  def removeIndex(name: String): UIO[Boolean]
  // def calls: ZStream[Any, Nothing, String]
}

object Log {

  def findLogs(from: Int, size: Int, search: Option[String]) =
    ZIO.serviceWithZIO[Log](_.findLogs(from, size, search))

  def countLogs(search: Option[String]) = 
    ZIO.serviceWithZIO[Log](_.countLogs(search))

  def removeIndex(name: String) =
    ZIO.serviceWithZIO[Log](_.removeIndex(name))

//  def calls =  ZStream.accessStream(_.get.calls)

  val live = ZLayer.fromFunction(LogLive.apply _)

}

case class LogLive(
    elastic: Elastic
) extends Log {

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
      .map(domain.Log.fromMap _)

  def countLogs(txt: Option[String]) = elastic.count(searchOption(txt))
  
  def removeIndex(name: String) = ZIO.unit.as(false)

}
