package actors


import akka.actor.{Actor, ActorRef}
import game._
import play.api.libs.json._
import game.Snake
import game.Point
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.libs.concurrent.Akka
import play.api.Logger


class PlayerActor(out:ActorRef) extends Actor with GameSpace{

  var moveQueue : List[Direction] = List()
  var snake = Snake(List(Point(0,0),Point(0,1),Point(0,2),Point(0,3)),Forwards)
  var previousPoints : Set[Point] = Set.empty

  implicit val pointWrites : Writes[Point] = new Writes[Point] {
    def writes(p: Point): JsValue = new JsArray(List(new JsNumber(p.x), new JsNumber(p.y)))
  }


  //TODO more efficient to do this with a fold?
  def aliveSnakesToPoints(snakes : Iterable[Snake]) : Set[Point] =
    snakes.filter(s => s.isAlive).map(s => s.points).flatten.toSet

  def receive = {
    case "l" => moveQueue = moveQueue :+ Left
    case "r" => moveQueue = moveQueue :+ Right
    case GetSnakesMsg =>  {
      var move : Direction = Forwards
      if(!moveQueue.isEmpty){
        move = moveQueue.head
        moveQueue = moveQueue.drop(1)
      }
      snake = snake.copy(facing = move).tick
      sender ! snake
    }
    case ReportSnakesMsg(mySnake,otherSnakes,food) => {

      val newPoints : Set[Point] = aliveSnakesToPoints(otherSnakes ++ List(mySnake)) ++ food
      val unchanged = previousPoints & newPoints
      val added = newPoints &~ unchanged
      val deleted = previousPoints &~ unchanged
      snake = mySnake
      previousPoints = newPoints
      out ! Json.toJson(List(added,deleted))
    }
  }

  override def preStart = {
    Logger.debug(self.path.toString)
    Actors.gameActor ! RegisterPlayerMsg
  }
}

