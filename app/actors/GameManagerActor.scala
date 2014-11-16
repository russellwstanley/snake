package actors

import akka.actor.{Terminated, Props, ActorRef, Actor}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class GameManagerActor extends Actor{

  var gameId = -1;
  var games : List[GameHolder] = List.empty
  var watchers  : Set[ActorRef] = Set.empty

  def getNewGameId : Int = {
    gameId = gameId +1
    return gameId
  }

  def receive = {
    case CreateGameMsg(name) => {
      val gameId = getNewGameId
      val game = Akka.system.actorOf(Props[SnakeGameActor], name = gameId.toString)
      Akka.system.scheduler.schedule(0.millisecond,100.millisecond,game,TickMsg)
      games = GameHolder(name,game) :: games
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
    case Terminated => {
      watchers = watchers - sender
    }
  }
}

