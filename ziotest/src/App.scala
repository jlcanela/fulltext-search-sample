import zio._
import Console._
import zio.metrics.MetricClient

object Cli extends ZIOAppDefault {

    override def run: ZIO[Environment with ZEnv with Has[ZIOAppArgs],Any,Any] = for {
    args <- ZIOAppArgs.getArgs
    exit <- myApp(args.toList) 
} yield exit


 def myApp(args: List[String]): URIO[ZEnv,ExitCode] = for {  
        _ <- ZIO.log("I’m a logger")     
        _ <- printLine("hello").orDie @@ ZIOMetric.count("name")
        _ <- ZIOMetric.setGauge("mygauge")(ZIO.succeed(2.0))
        metric = MetricClient.unsafeSnapshot
        _ <- printLine(s"metric: $metric").orDie
        _ <- ZIO.logSpan("I’m spanning") { for {
          // _ <- printLine("spanning")
            _ <- ZIO.log("I will go")
          _ <- ZIO.log("I’m an insider")
        } yield ()
            
        }
    } yield ExitCode(0)
}