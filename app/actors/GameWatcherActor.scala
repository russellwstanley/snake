package actors
import akka.actor.{Actor, ActorRef}
import play.api.libs.json._
import play.api.Play.current
import game.Point
import play.api.libs.concurrent.Akka
import play.api.Logger

class GameWatcherActor(gameId:String,out:ActorRef) extends Actor {

  var previousPoints : Set[Point] = Set.empty

  implicit val pointWrites : Writes[Point] = new Writes[Point] {
    def writes(p: Point): JsValue = new JsArray(List(new JsNumber(p.x), new JsNumber(p.y)))
  }

  //TODO duplication with PlayerActor
  def receive = {
    case "foo" =>
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
    Akka.system.actorSelection("/user/"+gameId) ! RegisterWatcherMsg
  }

}
