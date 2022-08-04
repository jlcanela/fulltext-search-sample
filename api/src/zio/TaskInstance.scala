package com.sksamuel.elastic4s.zio.instances

import zio.{Task, ZIO}
import com.sksamuel.elastic4s.{ElasticRequest, Executor, Functor, HttpClient, HttpResponse}

trait TaskInstances {
  implicit val taskFunctor: Functor[Task] = new Functor[Task] {
    override def map[A, B](fa: Task[A])(f: A => B): Task[B] = fa.map(f)
  }

  implicit val taskExecutor: Executor[Task] = new Executor[Task] {
    override def exec(client: HttpClient, request: ElasticRequest): Task[HttpResponse] =
      ZIO.asyncZIO { cb =>
        ZIO.attempt(client.send(request, v => cb(ZIO.fromEither(v))))
      }
  }
}