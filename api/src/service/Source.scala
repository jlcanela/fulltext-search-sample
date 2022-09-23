package service 

import zio._
import zio.query._

sealed trait ES_Search extends Request[Throwable, List[domain.Log]]
case class GetResult(offset: Long, size: Long) extends ES_Search

object Source {

  def exec(req: ES_Search): ZIO[Log, Throwable, List[domain.Log]] = req match {
    case GetResult(offset, size) => Log.findLogs(offset.toInt, size.toInt, None)
    //case GetCountResult(offset, size) => Search.find(offset, size)
    //case GetCount => Search.find(10, 2)
  }

  def execChunk(chunk: Chunk[ES_Search]): ZIO[Log with Console, Throwable, Chunk[List[domain.Log]]] = 
    for {
      _ <- ZIO.log(s"exec Chunk: ${chunk.toString()}")
      res <- ZIO.foreach(chunk)(exec) 
    } yield res
  
  def toKey(r: List[domain.Log]): ES_Search = {
    GetResult(10, 2)
  } 

  lazy val BatchedSearchDataSource: DataSource[Log with Console, ES_Search] =
    DataSource.fromFunctionBatchedWithZIO("searchB")(execChunk _, toKey _)
}
