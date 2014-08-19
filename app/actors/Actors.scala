package actors

import play.api.Play.current
import akka.actor.{Terminated, Actor, ActorRef}
import game._
import play.api.libs.json._
import play.api.libs.concurrent.Akka
import play.api.Logger
import scala.util.Random
import play.api.libs.functional.syntax._
import game.Point
import play.api.libs.json.JsArray
import game.Snake
import akka.actor.Terminated

/**
 * Created by russell on 10/08/14.
 */
trait GameSpace{

  implicit val space = new Space  {
    def leftBounds : Int = 0
    def rightBounds : Int = 60
    def upBounds : Int = 0
    def downBounds : Int = 60
  }

}

class PlayerActor(out:ActorRef) extends Actor with GameSpace{

  var moveQueue : List[Direction] = List()
  var snake = Snake(List(Point(0,0),Point(0,1),Point(0,2),Point(0,3)),Forwards)

  implicit val pointWrites : Writes[Point] = new Writes[Point] {
    def writes(p: Point): JsValue = new JsArray(List(new JsNumber(p.x), new JsNumber(p.y)))
  }



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
      //TODO more efficient to do this with a fold?
      def aliveSnakesToPoints(snakes : Iterable[Snake]) : List[List[Point]] = snakes.filter(s => s.isAlive).map(s => s.points).toList
      snake = mySnake
      if(snake.isAlive) {
        //TODO this is ugly
        out ! Json.toJson(((snake.points :: aliveSnakesToPoints(otherSnakes)) :+ food).flatten )
      }
      else  {
        out ! Json.toJson((aliveSnakesToPoints(otherSnakes) :+ food).flatten )
      }
    }
  }

  override def preStart = {
    Akka.system.actorSelection("/user/game") ! RegisterPlayerMsg
  }
}

object AddSnakeRequest
object GatherMovesRequest
case class ReportSnakesMsg(mySnake : Snake, otherSnakes : Iterable[Snake], food  :List[Point])
object GetSnakesMsg
object RegisterPlayerMsg
object GetPlayersMsg
object TickMsg
case class GetPlayersResponse(players : Set[ActorRef])

//class Registrar extends Actor{
//  var players : Set[ActorRef] = Set()
//  def receive = {
//    case RegisterPlayerMsg => {
//      context.watch(sender)
//      players = players + sender
//    }
//    case Terminated(actor) => players = players - actor
//    case GetPlayersMsg => sender ! GetPlayersResponse(players)
//  }
//}

class SnakeGameActor extends  Actor with ProcessSnakes with GameSpace{

  var requestedSnakes : Set[ActorRef] = Set.empty
  var players : Set[ActorRef] = Set()
  var snakes : Map[ActorRef,Snake] = Map.empty
  var food : List[Point] = List.empty
  val chanceOfNewFood = 0.02
  val minFood = 1
  val maxFood = 5

  private def isNewFood : Boolean = food.size match{
    case s if s < minFood => true
    case s if s >= maxFood => false
    case _ => Random.nextFloat() < chanceOfNewFood
  }

  private def reportNextGameStateToPlayers  = {
    val(fedSnakes,remainingFood) = resolveCollisionsWithFood(resolveCollisionsWithSnakes(snakes),food)
    food = remainingFood
    if (isNewFood) food  = generateNewFood(fedSnakes.values,food,space)
    fedSnakes.foreach{
      case(ref,snake) => ref ! ReportSnakesMsg(fedSnakes(ref),(fedSnakes - ref).values,food)
    }
  }

  def receive = waiting

  def waiting : Receive = {
    case RegisterPlayerMsg => {
      context.watch(sender)
      players = players + sender
    }
    case TickMsg => {
      if(players.size > 0){
        requestedSnakes =  players
        snakes = Map.empty
        context.become(calculateSnakes)
        players.foreach(player=>player ! GetSnakesMsg)
      }
    }
    case Terminated(actor) => players = players - actor
  }


  def calculateSnakes : Receive = {
    case snake : Snake => {
      requestedSnakes = requestedSnakes - sender
      snakes = snakes + (sender->snake)
      if(requestedSnakes.isEmpty) {
        reportNextGameStateToPlayers
        context.become(waiting)
      }
    }
    case Terminated(actor) => {
      players = players - actor
      requestedSnakes = requestedSnakes - actor
      if(requestedSnakes.isEmpty) {
        reportNextGameStateToPlayers
        context.become(waiting)
      }
    }
  }
}

