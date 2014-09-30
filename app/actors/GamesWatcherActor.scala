package actors

import akka.actor.{ActorRef, Actor}
import play.api.libs.json.Json
import game.SnakeGame

class GamesWatcherActor(out:ActorRef) extends Actor{

 //implicit val gamesWrites = Json.writes[Iterable[SnakeGame]]

  def receive = {
    case GamesListMsg(games) => out ! Json.toJson(games.map(_.path.toString))
  }

  override def preStart = {
    Actors.gameManagerActor ! RegisterWatcherMsg
  }

}
