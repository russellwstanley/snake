package actors

import akka.actor.{ActorRef, Actor}
import play.api.libs.json.{Writes, Json}
import game.SnakeGame
import play.api.Logger

class GamesWatcherActor(out:ActorRef) extends Actor{

  case class FormattedGame(name:String,id : String)
  implicit val gamesHolderWrites = Json.writes[FormattedGame]



  def receive = {
    case GamesListMsg(games) => {
      Logger.debug(this.getClass.toString + " GamesList")
      out ! Json.toJson(games.map{
      case GameHolder(name,ref) => FormattedGame(name,ref.path.name)

    })
  }
  }

  override def preStart = {
    Actors.gameManagerActor ! RegisterWatcherMsg
    Actors.gameManagerActor ! GetGamesMsg
  }

}
