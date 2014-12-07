package actors

import akka.actor.{ActorRef, Actor}
import game._
import game.Player
import akka.actor.Terminated
import scala.Some
import play.api.Logger

case class PlayerHolder(ref : ActorRef, player : Player, moveQueue : List[Direction] = Nil){

    def pushMove(move: Direction): PlayerHolder = copy(moveQueue = moveQueue :+ move)

    def popMove: PlayerHolder = moveQueue match {
      case Nil => copy()
      case head :: tail => copy(moveQueue = tail)
    }

    def move: Direction = moveQueue match {
      case Nil => Forwards
      case head :: tail => head
    }
}

class SnakeGameActor extends WatcherActor {

  var players: scala.collection.mutable.Map[String,PlayerHolder] = scala.collection.mutable.Map.empty
  var game = SnakeGame[Player]("testid", "test")

  def getMoves() : Map[Player,Direction] ={
    players.map{
      case (id, holder) => {
        val move = holder.move
        players += (id -> holder.popMove)
        holder.player -> move
      }
    }.toMap
  }

  def getByActorRef(ref : ActorRef) : Option[(String,PlayerHolder)] = {
    players.find{
      case(foundRef,player) => foundRef == ref
    }
  }


  def receive = handleWatching orElse {
    case RegisterPlayerMsg(player) => {
      context.watch(sender)
      if(!players.contains(player.id)) game = game.copy(state = game.state + player)
      players += (player.id -> PlayerHolder(sender,player))
    }
    case TickMsg => {
      val moves = getMoves
      game = game.next(moves)
      players.values.foreach(holder => holder.ref ! ReportStateMsg(game.state))
      watchers.foreach(ref => ref ! ReportStateMsg(game.state))
    }
    case MoveMsg(player,move) => players.get(player.id) match {
      case Some(holder) =>  players += (player.id -> holder.pushMove(move))
      case None => 
    }
    case Terminated(sender) => {
      getByActorRef(sender)match {
        case Some((id,_)) => players.remove(id)
        case None => //do nothing
      }
    }
  }
}

