package actors


import akka.actor.{Actor, ActorRef}
import game._
import play.api.libs.json._
import game.Snake
import game.Point
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.Logger


class PlayerActor(out:ActorRef) extends Actor with GameSpace{

  var previousPoints : Set[Point] = Set.empty

  implicit val pointWrites : Writes[Point] = new Writes[Point] {
    def writes(p: Point): JsValue = new JsArray(List(new JsNumber(p.x), new JsNumber(p.y)))
  }


  //TODO more efficient to do this with a fold?
  def aliveSnakesToPoints(snakes : Iterable[Snake]) : Set[Point] =
    snakes.filter(s => s.isAlive).map(s => s.points).flatten.toSet

  def receive = {
    case "l" => Actors.gameActor ! Left
    case "r" => Actors.gameActor ! Right
    case ReportSnakesMsg(mySnake,otherSnakes,food) => {
      val newPoints : Set[Point] = aliveSnakesToPoints(otherSnakes ++ List(mySnake)) ++ food
      val unchanged = previousPoints & newPoints
      val added = newPoints &~ unchanged
      val deleted = previousPoints &~ unchanged
      previousPoints = newPoints
      out ! Json.toJson(List(added,deleted))
    }
  }

  override def preStart = {
    Logger.debug(self.path.toString)
    Actors.gameActor ! RegisterPlayerMsg
  }
}

