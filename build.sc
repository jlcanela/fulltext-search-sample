import mill._, scalalib._

import $ivy.`net.sourceforge.plantuml:plantuml:8059`
import net.sourceforge.plantuml.SourceFileReader;
import java.io.File

object data extends ScalaModule {
  def scalaVersion = "2.13.6"

  def ivyDeps = Agg(
    ivy"dev.zio::zio:2.0.0-M4",
    ivy"dev.zio::zio-nio:1.0.0-RC11",
    ivy"net.sourceforge.plantuml:plantuml:8059",
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
