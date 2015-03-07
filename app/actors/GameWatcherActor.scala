package actors

import akka.actor.{Actor, ActorRef}
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.json._

case class ColoredPoint(x: Int, y: Int, c: String)

class GameWatcherActor(gameId: String, out: ActorRef) extends Actor {

  val FOOD_COLOR = "#000000";
  var previousPoints: Set[ColoredPoint] = Set.empty

  implicit val pointWrites = Json.writes[ColoredPoint]

  def receive = {
    case ReportStateMsg(state) => {
      val newPoints: Set[ColoredPoint] = {
        state.snakes.flatMap {
          case (player, snake) => snake.points.map {
            point => ColoredPoint(point.x, point.y, player.color.toString)
          }
        }.toSet ++ state.food.map(point => ColoredPoint(point.x, point.y, FOOD_COLOR))
      }
      val unchanged = previousPoints & newPoints
      val added = newPoints &~ unchanged
      val deleted = previousPoints &~ unchanged
      previousPoints = newPoints
      out ! Json.toJson(List(added, deleted))
    }
  }


  override def preStart = {
    Akka.system.actorSelection("/user/" + gameId) ! RegisterWatcherMsg
  }

}
