package actors

import akka.actor.ActorRef
import game._


class PlayerActor(player: Player, gameId: String, out: ActorRef) extends GameWatcherActor(gameId, out) {

  val playMoves: PartialFunction[Any, Unit] = {
    case "l" => Actors.gameActor(gameId) ! MoveMsg(player, Left)
    case "r" => Actors.gameActor(gameId) ! MoveMsg(player, Right)
  }

  override val receive = playMoves orElse super.receive

  override def preStart = {
    Actors.gameActor(gameId) ! RegisterPlayerMsg(player)
  }
}

