package actors

import akka.actor.{ActorRef, Terminated}
import game.{Player, _}

case class PlayerHolder(ref: ActorRef, player: Player, moveQueue: List[Direction] = Nil) {

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

  var players: scala.collection.mutable.Map[String, PlayerHolder] = scala.collection.mutable.Map.empty
  var game = SnakeGame[Player]("testid", "test")

  def getMoves(): Map[Player, Direction] = {
    players.map {
      case (id, holder) => {
        val move = holder.move
        players += (id -> holder.popMove)
        holder.player -> move
      }
    }.toMap
  }

  def getByActorRef(ref: ActorRef): Option[(String, PlayerHolder)] = {
    players.find {
      case (id, holder) => holder.ref == ref
    }
  }


  def receive = handleWatching orElse {
    case RegisterPlayerMsg(player) => {
      context.watch(sender)
      if (!game.state.snakes.contains(player)) game = game.copy(state = game.state + player)
      players += (player.id -> PlayerHolder(sender, player))
    }
    case TickMsg => {
      val moves = getMoves
      game = game.next(moves)
      players.values.foreach(holder => holder.ref ! ReportStateMsg(game.state))
      watchers.foreach(ref => ref ! ReportStateMsg(game.state))
      game = game.cleanDeadSnakes
    }
    case MoveMsg(player, move) => players.get(player.id) match {
      case Some(holder) => players += (player.id -> holder.pushMove(move))
      case None =>
    }

    case Terminated(sender) => {
      //todo if a player is terminated when they are also a watcher they will never be removed from the list
      getByActorRef(sender) match {
        case Some((id, holder)) => players.remove(id)
        case None => //do nothing
      }
    }
  }
}

