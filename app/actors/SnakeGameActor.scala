package actors

import akka.actor.{ActorRef, Actor}
import game._
import game.Player
import akka.actor.Terminated
import scala.Some
import play.api.Logger

case class PlayerHolder(ref : ActorRef, player : Player)

class SnakeGameActor extends WatcherActor {

  var players: scala.collection.mutable.Map[String,PlayerHolder] = scala.collection.mutable.Map.empty
  var game = SnakeGame[String]("testid", "test")

  def getMoves() : Map[String,Direction] ={
    players.map{
      case (id, PlayerHolder(ref,player)) => {
        val move = player.move
        players += (id -> PlayerHolder(ref ,player.popMove))
        id -> move
      }
    }.toMap
  }

  def getByActorRef(ref : ActorRef) : Option[(String,PlayerHolder)] = {
    players.find{
      case(foundRef,player) => foundRef == ref
    }
  }


  def receive = handleWatching orElse {
    case RegisterPlayerMsg(id) => {
      context.watch(sender)
      if(!players.contains(id)) game = game.copy(state = game.state + id)
      players += (id -> PlayerHolder(sender,Player(id)))
    }
    case TickMsg => {
      val moves = getMoves
      game = game.next(moves)
      players.values.foreach(holder => holder.ref ! ReportStateMsg(game.state))
      watchers.foreach(ref => ref ! ReportStateMsg(game.state))
    }
    case MoveMsg(id,move) => players.get(id) match {
      case Some(PlayerHolder(ref,player)) =>  players += (id -> PlayerHolder(sender,player.pushMove(move)))
      case None => 
    }
    case Terminated(sender) => {
      getByActorRef(sender)match {
        case Some((id,PlayerHolder(ref,player))) => players.remove(id)
        case None => //do nothing
      }
    }
  }
}

