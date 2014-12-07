package actors

import akka.actor.{Terminated, Props, ActorRef, Actor}
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class GameManagerActor extends WatcherActor{

  var gameId = -1;
  var games : List[GameHolder] = List.empty

  def getNewGameId : Int = {
    gameId = gameId +1
    return gameId
  }

  def receive = handleWatching orElse {
    case CreateGameMsg(name) => {
      Logger.debug(this.getClass.toString + "CreateGamesMsg")
      val gameId = getNewGameId
      val game = Akka.system.actorOf(Props[SnakeGameActor], name = gameId.toString)
      Akka.system.scheduler.schedule(0.millisecond,100.millisecond,game,TickMsg)
      games = GameHolder(name,game) :: games
      watchers.foreach(ref => ref ! GamesListMsg(games))
    }
    case GetGamesMsg => {
      Logger.debug(this.getClass.toString + " GetGamesMsg")
      sender ! GamesListMsg(games)
    }
  }
}

