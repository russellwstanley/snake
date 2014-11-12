package actors
import play.api.Play.current
import akka.actor.{Actor, ActorRef}
import game._
import play.api.libs.json._
import game.AliveSnake
import game.Point
import play.api.libs.concurrent.Akka


class PlayerActor(gameId:String,out:ActorRef) extends Actor{

  var previousPoints : Set[Point] = Set.empty

  implicit val pointWrites : Writes[Point] = new Writes[Point] {
    def writes(p: Point): JsValue = new JsArray(List(new JsNumber(p.x), new JsNumber(p.y)))
  }

  def receive = {
    case "l" => Akka.system.actorSelection("/user/"+gameId)  ! Left
    case "r" => Akka.system.actorSelection("/user/"+gameId)  ! Right
    case ReportStateMsg(state) => {
      val newPoints : Set[Point] = state.snakePoints.toSet ++ state.food
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

