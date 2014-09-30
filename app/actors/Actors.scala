package actors

import play.api.Play.current
import akka.actor._
import game._
import play.api.libs.concurrent.Akka
import game.Point
import game.Snake

object Actors{
  val gameManagerName = "game_manager"
  val gameName = "game"
  def gameManagerActor = Akka.system.actorSelection("/user/"+gameManagerName)
  def gameActor = Akka.system.actorSelection("/user/"+Actors.gameName)
}

trait GameSpace{

  implicit val space = new Space  {
    def leftBounds : Int = 0
    def rightBounds : Int = 60
    def upBounds : Int = 0
    def downBounds : Int = 60
  }

}

object AddSnakeRequest
object GatherMovesRequest
case class ReportSnakesMsg(mySnake : Snake, otherSnakes : Iterable[Snake], food  :List[Point])
object GetSnakesMsg
object RegisterPlayerMsg
object RegisterWatcherMsg
object GetPlayersMsg
object TickMsg
case class GetPlayersResponse(players : Set[ActorRef])
case class CreateGameMsg(name : String)
case class GetGamesMsg()
case class GamesListMsg(games : Set[ActorRef])



