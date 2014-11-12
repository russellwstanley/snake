package actors

import akka.actor.{ActorRef, Actor}
import game._
import game.Player
import akka.actor.Terminated
import scala.Some
import play.api.Logger

class SnakeGameActor extends Actor {

  var players: Map[ActorRef, Player] = Map.empty
  var watchers: Set[ActorRef] = Set[ActorRef]()//TODO duplicated from GamesWatcher??
  var game = SnakeGame[ActorRef]("testid", "test")

  //would be cleaner with mutable state
  def nextPlayerMoves: (Map[ActorRef, Player], Map[ActorRef, Direction]) = {
    players.foldLeft(Map[ActorRef, Player](), Map[ActorRef, Direction]()) {
      case ((newPlayers, moves), (id, player)) =>
        val move = player.move
        val newPlayer = player.popMove
        ((newPlayers + (id -> newPlayer)), moves + (id -> move))
    }
  }

  def receive = {
    case RegisterPlayerMsg => {
      Logger.debug("REGISTER PLAYER")
      context.watch(sender)
      players = players + (sender->Player())
      game = game.copy(state = game.state + sender)
    }
    case RegisterWatcherMsg =>{
      Logger.debug("REGISTER WATCHER")
      context.watch(sender)
      watchers = watchers + sender
    }
    case TickMsg => {
      //the immutable style makes this ungainly
      val (newPlayers,moves) = nextPlayerMoves
      players = newPlayers
      game = game.next(moves)
      players.keys.foreach(ref => ref ! ReportStateMsg(game.state))
      watchers.foreach(ref => ref ! ReportStateMsg(game.state))
    }
    case move: Direction => players.get(sender) match {
      case Some(player) => players = players + (sender -> player.pushMove(move))
      case None => 
    }
    case Terminated(actor) => {
      Logger.debug("terminated "+actor)
      players = players - actor
      watchers = watchers - actor
    }
  }


}

