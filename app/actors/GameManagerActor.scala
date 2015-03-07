package actors

import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class GameManagerActor extends WatcherActor {

  var gameId = -1;
  var games: List[GameHolder] = List.empty

  def getNewGameId: Int = {
    gameId = gameId + 1
    gameId
  }

  def receive = handleWatching orElse {
    case CreateGameMsg(name) => {
      val gameId = getNewGameId
      val game = Akka.system.actorOf(Props[SnakeGameActor], name = gameId.toString)
      Akka.system.scheduler.schedule(0.millisecond, 100.millisecond, game, TickMsg)
      games = GameHolder(name, game) :: games
      watchers.foreach(ref => ref ! GamesListMsg(games))
    }
    case GetGamesMsg => {
      sender ! GamesListMsg(games)
    }
  }
}

