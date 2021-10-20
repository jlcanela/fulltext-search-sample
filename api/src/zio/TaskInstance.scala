package com.sksamuel.elastic4s.zio.instances

import com.sksamuel.elastic4s.{ElasticRequest, ElasticClient, Executor, Functor, HttpClient, HttpResponse}

import zio.Task

trait TaskInstances {
  implicit val taskFunctor: Functor[Task] = new Functor[Task] {
    override def map[A, B](fa: Task[A])(f: A => B): Task[B] = fa.map(f)
  }

  implicit val taskExecutor: Executor[Task] = new Executor[Task] {
    override def exec(client: HttpClient, request: ElasticRequest): Task[HttpResponse] =
      Task.effectAsyncM { cb =>
        val clazz = client.getClass.getName
        //val headers = Map("Authorization" -> "Basic ZWxhc3RpYzpzb21ldGhpbmdzZWNyZXQ=")
        //val req = ElasticRequest(request.method, request.endpoint, request.params ++ headers, request.entity)
        Task.effect(client.send(request, v => cb(Task.fromEither(v))))
      }
  }
}
