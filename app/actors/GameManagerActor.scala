package actors

import akka.actor.Actor
import game.SnakeGame

class GameManagerActor extends Actor{

  var gameId = -1;
  var games : Map[Int,SnakeGame] = Map.empty

  def getNewGameId : Int = {
    gameId = gameId +1
    return gameId
  }

  def receive = {
    case CreateGameMsg(game) => {
      val gameId = getNewGameId
      games = games + (gameId->game)
      sender ! gameId
    }
    case GetGamesMsg() => {
      sender ! GamesListMsg(games.values)
    }
  }
}

