import zio._

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.requests.searches.SearchHit

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

import model.Log

object ElasticService {

    trait ElasticService {
        def search(req: SearchRequest): Task[Either[RequestFailure, Array[SearchHit]]]
    }
  
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    def make: ZLayer[Any, Throwable, Has[ElasticService.ElasticService]] = ZLayer.fromEffect(ZIO.succeed(ElasticLive()))

}

case class ElasticLive() extends ElasticService.ElasticService {

    def connect = ZIO.bracket { for {
            client <- Elastic.createClient(ElasticConfig.get)
        } yield ElasticClient(client)
    }(client => ZIO.effectTotal(client.close))

    def search(req: SearchRequest): ZIO[Any, Throwable, Either[RequestFailure, Array[SearchHit]]] = connect { client =>
        for {
            res <- client.execute(req)
            hits <- res match {
                case failure: RequestFailure => ZIO.succeed(Left(failure)) 
                case results: RequestSuccess[SearchResponse] => ZIO.succeed(Right(results.result.hits.hits))  
            }
        } yield hits
    }
}