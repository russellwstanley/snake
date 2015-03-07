package actors

import akka.actor.{Actor, ActorRef}
import play.api.Logger
import play.api.libs.json.Json

class GamesWatcherActor(out: ActorRef) extends Actor {

  case class FormattedGame(name: String, id: String)

  implicit val gamesHolderWrites = Json.writes[FormattedGame]


  def receive = {
    case GamesListMsg(games) => {
      Logger.debug(this.getClass.toString + " GamesList")
      out ! Json.toJson(games.map {
        case GameHolder(name, ref) => FormattedGame(name, ref.path.name)

      })
    }
  }

  override def preStart = {
    Actors.gameManagerActor ! RegisterWatcherMsg
    Actors.gameManagerActor ! GetGamesMsg
  }

}
