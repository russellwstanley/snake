package actors

import akka.actor.{Props, ActorRef, Actor}
import game.SnakeGame
import play.api.libs.concurrent.Akka
import play.api.Play.current

class GameManagerActor extends Actor{

  var gameId = -1;
  var games : Set[ActorRef] = Set.empty
  var watchers  : Set[ActorRef] = Set.empty

  def getNewGameId : Int = {
    gameId = gameId +1
    return gameId
  }

  def receive = {
    case CreateGameMsg(name) => {
      val gameId = getNewGameId
      games = games + Akka.system.actorOf(Props[SnakeGameActor], name = gameId.toString)
      watchers.foreach(ref => ref ! GamesListMsg(games))
      sender ! gameId
    }
    case RegisterWatcherMsg => {
      watchers = watchers + sender
      sender ! GamesListMsg(games)
    }
    case GetGamesMsg() => {
      sender ! GamesListMsg(games)
    }
  }
}

