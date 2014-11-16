package actors

import akka.actor.{ActorRef, Actor}
import game._
import game.Player
import akka.actor.Terminated
import scala.Some
import play.api.Logger

class SnakeGameActor extends Actor {

  var players: scala.collection.mutable.Map[ActorRef, Player] = scala.collection.mutable.Map.empty
  var watchers: Set[ActorRef] = Set[ActorRef]()//TODO duplicated from GamesManager??
  var game = SnakeGame[ActorRef]("testid", "test")

//  def nextPlayerMoves: (Map[ActorRef, Player], Map[ActorRef, Direction]) = {
//    players.foldLeft(Map[ActorRef, Player](), Map[ActorRef, Direction]()) {
//      case ((newPlayers, moves), (id, player)) =>
//        val move = player.move
//        val newPlayer = player.popMove
//        ((newPlayers + (id -> newPlayer)), moves + (id -> move))
//    }
//  }j


  //FORTALK why is players mutable

  def getMoves() : Map[ActorRef,Direction] ={
    players.map{
      case (ref,player) => {
        val move = player.move
        players += ref -> player.popMove
        ref -> move
      }
    }.toMap
  }


  def receive = {
    case RegisterPlayerMsg => {
      context.watch(sender)
      players = players + (sender->Player())
      game = game.copy(state = game.state + sender)
    }
    case RegisterWatcherMsg =>{
      context.watch(sender)
      watchers = watchers + sender
    }
    case TickMsg => {
      //the immutable style makes this ungainly
      val moves = getMoves
      game = game.next(moves)
      players.keys.foreach(ref => ref ! ReportStateMsg(game.state))
      watchers.foreach(ref => ref ! ReportStateMsg(game.state))
    }
    case move: Direction => players.get(sender) match {
      case Some(player) => players = players + (sender -> player.pushMove(move))
      case None => 
    }
    case Terminated(actor) => {
      players = players - actor
      watchers = watchers - actor
    }
  }


}

