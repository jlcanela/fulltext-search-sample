import example.ExampleData._
import zio.stream.ZStream
import zio.{ Has, Hub, Ref, UIO, URIO, ZLayer, ZIO }
import model.Log

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.SearchHit
import zio.console

object LogService {

  trait LogService {
    def findLogs(from: Int, size: Int): UIO[List[Log]]
  }

  def findLogs(from: Int, size: Int): URIO[Has[LogService], List[Log]] =
    URIO.serviceWith(_.findLogs(from, size))

  def make: ZLayer[Has[ElasticService.ElasticService], Nothing, Has[LogService]] = ZLayer.fromEffect(for {
    elastic <- ZIO.service[ElasticService.ElasticService]
  } yield LogLive(elastic))
  
}

case class LogLive(elastic: ElasticService.ElasticService) extends LogService.LogService {

    def findLogs(from: Int, size: Int): UIO[List[Log]] = (for {
        hits <- ZIO.absolve(elastic.search {
            search("web").query(must(matchAllQuery()).filter(termQuery("uri", "php"))).from(from).size(size).sortByFieldAsc("datetime")
            // sourceInclude("gps", "populat*") sourceExclude("denonymn", "capit*")
        })
    } yield hits.toList.map(_.sourceAsMap).map(Log.fromMap _)).fold(x=> List(Log("","","","","","","","","","", "", x.toString)), x => x)

}
