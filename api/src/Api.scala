import zio._

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.requests.searches.SearchHit

import com.sksamuel.elastic4s.zio.instances._
import org.elasticsearch.client.RestClient
import org.apache.http.HttpHost
import org.apache.http.message.BasicHeader
import org.apache.http.Header
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.auth.AuthScope
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback
import org.apache.http.client.config.RequestConfig

object Api {

    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    def createClient(host: String, port: Int, user: String, password: String, ssl: Boolean = false) = {
        import com.sksamuel.elastic4s.http.{NoOpHttpClientConfigCallback}

        lazy val provider = {
            val provider = new BasicCredentialsProvider
            val credentials = new UsernamePasswordCredentials(user, password)
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
        
        val formedHost = new HttpHost(host, port, if (ssl) "https" else "http")

        val restClientBuilder = RestClient.builder(formedHost)
        .setHttpClientConfigCallback(httpClientConfigCallback)
        .setRequestConfigCallback(requestConfigCallback)
        //.setDefaultHeaders(headers)
        JavaClient.fromRestClient(restClientBuilder.build())
    }

    def connect: ZIO.Release[Any, Throwable, ElasticClient] = ZIO.acquireReleaseWith { ZIO.attempt {
        val elasticHost = "localhost"
        val elasticPort = 9200
        val user = "elastic"
        val password = "somethingsecret"
        ElasticClient(createClient(elasticHost, elasticPort, user, password))
     }
    }(client => ZIO.succeed(client.close))

    def searchApi: ZIO[Any, Serializable, Array[SearchHit]] = for {
        resp <- connect { client => 
            client.execute {
                search("web").matchQuery("uri", "php").sortByFieldDesc("datetime")
            }
        }
        rr <- resp match {
            case failure: RequestFailure => ZIO.fail(failure) 
            case results: RequestSuccess[SearchResponse] => ZIO.succeed(results.result.hits.hits)            
        }
    } yield rr
}