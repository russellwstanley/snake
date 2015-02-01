package actors

import akka.actor.{Actor, ActorRef}
import game.{Player, GameState, Snake}
import play.api.libs.json.{JsPath, Writes}
import play.libs.Akka
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by russell on 2/1/15.
 */
class GameInfoActor(gameId: String, ref: ActorRef) extends Actor{

  implicit val snakeWrites : Writes[Map[Player,Snake]]

  implicit val gameStateWrites : Writes[GameState] = {
    def writes(state : GameState[Player]) = Json.obj(
      "snakes" -> state.snakes
    )
  }
  val lastState = None

  def receive = {
    case ReportStateMsg(state) if state != lastState =>
  }


  override def preStart = {
    Akka.system.actorSelection("/user/"+gameId) ! RegisterWatcherMsg
  }

}
