import mill._, scalalib._

import $ivy.`net.sourceforge.plantuml:plantuml:8059`
import net.sourceforge.plantuml.SourceFileReader;
import java.io.File

import mill.modules.Assembly

object batch extends ScalaModule { outer =>
  def scalaVersion = "2.12.15"
  def scalacOptions =
    Seq("-encoding", "utf-8", "-explaintypes", "-feature", "-deprecation")

  def ivySparkDeps = Agg(
    ivy"org.apache.spark::spark-sql:3.1.2"
      .exclude("org.slf4j" -> "slf4j-log4j12"),
    ivy"org.slf4j:slf4j-api:1.7.16",
    ivy"org.slf4j:slf4j-log4j12:1.7.16"
  )

  def ivyDeps = Agg(
    ivy"com.lihaoyi::upickle:0.9.7",
    ivy"org.elasticsearch::elasticsearch-spark-30:7.15.1",
    ivy"dev.zio::zio:2.0.0-M4"
  )

  def compileIvyDeps = ivySparkDeps

  def assemblyRules =
    Assembly.defaultRules ++
      Seq(
        "scala/.*",
        "org.slf4j.*",
        "org.apache.log4j.*"
      ).map(Assembly.Rule.ExcludePattern.apply)

  object standalone extends ScalaModule {
    def scalaVersion = outer.scalaVersion
    def moduleDeps = Seq(outer)
    def ivyDeps = outer.ivySparkDeps
    override def mainClass = T { Some("App") }
    def forkArgs = Seq("-Dspark.master=local[*]")
  }
}

object data extends ScalaModule {
  def scalaVersion = "2.13.6"

  def ivyDeps = Agg(
    ivy"dev.zio::zio:2.0.0-M4",
    ivy"dev.zio::zio-nio:1.0.0-RC11",
    //ivy"net.sourceforge.plantuml:plantuml:8059",
    ivy"com.lihaoyi::os-lib:0.7.8"
  )

  def genPuml() = T.command {
    println("Generating documentation ") 

    for {
        path <- os.list(os.pwd/"doc").filter(_.ext == "puml").map(_.relativeTo(os.pwd))
        file = path.toString
        res = os.proc("java", "-jar", "tools/plantuml.1.2021.12.jar", file).call()
    } yield path.toString
    
  }
}


object api extends ScalaModule {
  def scalaVersion = "2.13.6"

  def ivyDeps = Agg(
    ivy"dev.zio::zio:2.0.0-M4",
    ivy"dev.zio::zio-nio:1.0.0-RC11",
    ivy"com.sksamuel.elastic4s::elastic4s-client-esjava:7.15.0",
    ivy"com.google.guava:guava:31.0.1-jre",
  //  ivy"org.slf4j:slf4j-simple:1.7.32",
    ivy"ch.qos.logback:logback-classic:1.2.6"
  )

}
