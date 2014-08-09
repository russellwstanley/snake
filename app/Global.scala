import play.api.Play.current
import play.api.libs.concurrent.Akka
import akka.actor.Props
import controllers.{Registrar, SnakeGameActor}
import play.api.GlobalSettings
import play.api.Application
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global
object Global extends GlobalSettings{

  override def onStart(app : Application){
    val gameActor = Akka.system.actorOf(Props[SnakeGameActor], name = "game")
    val registrar = Akka.system.actorOf(Props[Registrar], name = "registrar")
    Akka.system.scheduler.schedule(0.millisecond,100.millisecond,gameActor,"tick")
  }

}

