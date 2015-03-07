package actors

import akka.actor.{Actor, ActorRef}
import play.api.libs.json._
import play.libs.Akka

case class GameInfoItem(name: String, color: String, length: Int)

class GameInfoActor(gameId: String, out: ActorRef) extends Actor {

  implicit val gameInfoItemReads = Json.writes[GameInfoItem]

  var lastState: Map[String, GameInfoItem] = Map.empty

  def receive = {
    case ReportStateMsg(state) => {
      val newState = state.snakes.map {
        case (player, snake) => player.id -> GameInfoItem(player.name, player.color, snake.length)
      }
      if (lastState != newState) out ! Json.toJson(newState.values)
      lastState = newState
    }
  }

  override def preStart = {
    Akka.system.actorSelection("/user/" + gameId) ! RegisterWatcherMsg
  }

}
