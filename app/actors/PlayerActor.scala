package actors
import play.api.Play.current

import akka.actor.{Actor, ActorRef}
import game._
import play.api.libs.json._
import game.AliveSnake
import game.Point
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.Logger
import play.api.libs.concurrent.Akka


class PlayerActor(gameId:String,out:ActorRef) extends Actor with GameSpace{

  var previousPoints : Set[Point] = Set.empty

  implicit val pointWrites : Writes[Point] = new Writes[Point] {
    def writes(p: Point): JsValue = new JsArray(List(new JsNumber(p.x), new JsNumber(p.y)))
  }

  //TODO should probably be making these calculations once per game rather than per player

  //TODO more efficient to do this with a fold?
  def aliveSnakesToPoints(snakes : Iterable[Snake]) : Set[Point] =
    snakes.map(s => s.points).flatten.toSet

  def receive = {
    case "l" => Akka.system.actorSelection("/user/"+gameId)  ! Left
    case "r" => Akka.system.actorSelection("/user/"+gameId)  ! Right
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
    Akka.system.actorSelection("/user/"+gameId) ! RegisterPlayerMsg
  }
}

