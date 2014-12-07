package actors

import akka.actor._
import game._
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka

trait WatcherActor extends Actor{
  var watchers  : Set[ActorRef] = Set.empty

  val handleWatching : PartialFunction[Any,Unit] = {

    case RegisterWatcherMsg => {
      Logger.debug(getClass().toString+" RegisterWatcher")
      context.watch(sender)
      watchers = watchers + sender
    }
    case Terminated(actor) if watchers.contains(actor) => watchers = watchers - actor
  }
}

object Actors{
  val gameManagerName = "game_manager"
  val gameName = "game"
  def gameManagerActor = Akka.system.actorSelection("/user/"+gameManagerName)
  def gameActor = Akka.system.actorSelection("/user/"+Actors.gameName)
}

object AddSnakeRequest
object GatherMovesRequest
case class ReportStateMsg(state : GameState[ActorRef] )
object GetSnakesMsg
object RegisterPlayerMsg
object RegisterWatcherMsg
object GetPlayersMsg
object GetGamesMsg
object TickMsg
case class GetPlayersResponse(players : Set[ActorRef])
case class CreateGameMsg(name : String)
case class GamesListMsg(games : List[GameHolder])
case class GameHolder(name: String, ref : ActorRef)



