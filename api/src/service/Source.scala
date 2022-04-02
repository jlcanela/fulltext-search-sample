import zio._
import zio.query._
import model.Log


sealed trait ES_Search extends Request[Throwable, List[Log]]
case class GetResult(offset: Long, size: Long) extends ES_Search

object Source {

  def exec(req: ES_Search): ZIO[LogService, Throwable, List[Log]] = req match {
    case GetResult(offset, size) => LogService.findLogs(offset.toInt, size.toInt, None)
    //case GetCountResult(offset, size) => Search.find(offset, size)
    //case GetCount => Search.find(10, 2)
  }

  def execChunk(chunk: Chunk[ES_Search]): ZIO[LogService with Console, Throwable, Chunk[List[Log]]] = 
    for {
      _ <- Console.printLine(chunk.toString)
      res <- ZIO.foreach(chunk)(exec) 
    } yield res
  
  def toKey(r: List[Log]): ES_Search = {
    println("toKey:", r.toString())
    GetResult(10, 2)
  } 

  lazy val BatchedSearchDataSource: DataSource[LogService with Console, ES_Search] =
    DataSource.fromFunctionBatchedWithM("searchB")(execChunk _, toKey _)
}
