package service

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

object ElasticConfig {

    def get = ElasticBase.ElasticConfig(
        elasticHost = "localhost",
        elasticPort = 9200,
        user = "elastic",
        password = "somethingsecret", 
        ssl = false)
}

trait ElasticBase {

    def createClient(config: ElasticBase.ElasticConfig): ZIO[Any, Throwable, JavaClient]
    
    def connect: ZIO.Release[Any, Throwable, ElasticClient]
}

object ElasticBase {
    
    case class ElasticConfig(elasticHost: String, elasticPort: Int, user: String, password: String, ssl: Boolean)
    
    def createClient(config: ElasticBase.ElasticConfig) = ZIO.serviceWithZIO[ElasticBase](_.createClient(config))

    def connect = ZIO.serviceWith[ElasticBase](_.connect)

    val live = ZLayer.fromFunction(ElasticBaseLive.apply _)

}

case class ElasticBaseLive(config: ElasticBase.ElasticConfig) extends ElasticBase {

    def connect = ZIO.acquireReleaseWith { for {
            client <- createClient(config)
            _      <- ZIO.logInfo("Elastic Client Created")
        } yield ElasticClient(client)
    }(client => ZIO.attemptBlockingIO(client.close).ignore)

     def createClient(config: ElasticBase.ElasticConfig) = ZIO.attemptBlockingIO {
        
        lazy val provider = {
            val provider = new BasicCredentialsProvider
            val credentials = new UsernamePasswordCredentials(config.user, config.password)
            provider.setCredentials(AuthScope.ANY, credentials)
            provider
        }

        val requestConfigCallback = new RequestConfigCallback  {
            override def customizeRequestConfig(requestConfigBuilder: RequestConfig.Builder) = {
                requestConfigBuilder
            }
        }

        val httpClientConfigCallback: HttpClientConfigCallback = new HttpClientConfigCallback {

          override def customizeHttpClient(builder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
              builder.setDefaultCredentialsProvider(provider)
          }

        } 
        
        val formedHost = new HttpHost(config.elasticHost, config.elasticPort, if (config.ssl) "https" else "http")

        val restClientBuilder = RestClient.builder(formedHost)
        .setHttpClientConfigCallback(httpClientConfigCallback)
        .setRequestConfigCallback(requestConfigCallback)

        JavaClient.fromRestClient(restClientBuilder.build())
    }
}
