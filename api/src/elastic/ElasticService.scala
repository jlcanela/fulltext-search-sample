package elastic 

import zio._

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.requests.searches.SearchHit
import com.sksamuel.elastic4s.requests.searches.queries.{Query => ESQuery}

import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback
import org.apache.http.client.config.RequestConfig
import org.apache.http.HttpHost
import org.apache.http.Header

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.auth.AuthScope

import com.sksamuel.elastic4s.requests.searches.SearchRequest
import com.sksamuel.elastic4s.zio.instances._
import com.sksamuel.elastic4s.requests.count.CountResponse

case class SearchResult(count: Long, hits: Array[SearchHit])

trait ElasticService {

    def search(req: SearchRequest): ZIO[Any, Throwable, SearchResult]
    def count(req: ESQuery): ZIO[Any, Throwable, Long]
    def removeIndex(name: String): Task[Boolean]

}

object ElasticService {

    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    trait ElasticError extends Exception
    
    object ElasticError {
        final case class Error(error: Throwable) extends ElasticError
        final case class Failure(failure: RequestFailure) extends ElasticError
        def mapError(err: Either[RequestFailure, Throwable]) = err.fold(Failure(_), Error(_))
    }

    def removeIndex(name: String) = ZIO.serviceWithZIO[ElasticService](_.removeIndex(name))
  
    val live = ZLayer.fromFunction(ElasticServiceLive.apply _)

}

case class ElasticServiceLive(elastic: Elastic) extends ElasticService {
    
    import com.sksamuel.elastic4s.ElasticDsl._
    import com.sksamuel.elastic4s.Indexes

    def search(req: SearchRequest) = elastic.connect { client =>
            for {
                res <- client.execute(req)
                hits <- res match {
                    case failure: RequestFailure => ZIO.succeed(Left(failure)) 
                    case results: RequestSuccess[SearchResponse] => ZIO.succeed(Right(SearchResult(results.result.hits.total.value, results.result.hits.hits)))  
                }
            } yield hits
        }
        .right.mapError(ElasticService.ElasticError.mapError(_))


    def count(req: ESQuery) = elastic.connect { client =>
        for {
                res <- client.execute(ElasticDsl.count(Indexes("web")).query(req))
                c <- res match {
                    case failure: RequestFailure => ZIO.succeed(Left(failure)) 
                    case results: RequestSuccess[CountResponse] => ZIO.succeed(Right(results.result.count))  
                }
        } yield c
    }.right.mapError(ElasticService.ElasticError.mapError(_))

    def removeIndex(index: String): Task[Boolean] = elastic.connect { client => for {
            _ <- client.execute {
                deleteIndex(index)
            }
        } yield (true)
    }
}
