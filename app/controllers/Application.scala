package controllers

import play.api._
import play.api.mvc._
import akka.actor._
import play.api.Play.current
import game._
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json

object Application extends Controller {

  def index = Action {
    Logger.debug("index")
    Ok(views.html.index("Snake"))
  }

  def socket = WebSocket.acceptWithActor[String,String]{ request => out => PlayerActor.props(out)}


}

object PlayerActor{



  def props(out : ActorRef) = Props(new PlayerActor(out))
}


class PlayerActor(out:ActorRef) extends Actor{

  var moveQueue : List[Direction] = List()
  var snake = Snake(List(Point(0,0),Point(0,1)),Forwards)

  implicit val space = new Space  {
    def leftBounds : Int = 0
    def rightBounds : Int = 60
    def upBounds : Int = 0
    def downBounds : Int = 60
  }
  implicit val pointWrites = Json.writes[Point]

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
    case ReportSnakesMsg(mySnake,otherSnakes) => {
      Logger.debug("reporting snakes to player")
      out ! Json.toJson(mySnake.points :: otherSnakes.map(s => s.points).toList).toString()
    }
  }

  override def preStart = {
     Akka.system.actorSelection("/user/registrar") ! "register"
  }
}

case class AddSnakeRequest()
case class GatherMovesRequest()
case class ReportSnakesMsg(mySnake : Snake, otherSnakes : Iterable[Snake])
case class GetSnakesMsg()

class Registrar extends Actor{
  var players : Set[ActorRef] = Set()
  def receive = {
    case "register" => {
      Logger.debug("player registered")
      context.watch(sender)

      players = players + sender
    }
    case Terminated(actor) => players = players - actor
    case "get" => sender ! players
  }

}

class SnakeGameActor extends  Actor{

  var requestedSnakes : Set[ActorRef] = Set.empty
  var snakes : scala.collection.mutable.Map[ActorRef,Snake] = scala.collection.mutable.Map.empty
  def receive = waiting
  //once all the players have posted moves the result is
  //calculated and immediately broadcast to all the players
  def waiting : Receive = {
    case "tick" => {
      Logger.debug("ticked")
      context.become(gatherPlayers)
      Logger.debug("gathering")
      Akka.system.actorSelection("/user/registrar") ! "get"
    }
  }

  def calculateSnakes : Receive = {
    case Snake(points,facing) => {
      requestedSnakes = requestedSnakes - sender
      snakes(sender) = Snake(points,facing)
      if(requestedSnakes.isEmpty) {
        Logger.debug("gathered")
        snakes.map{
          case(ref,snake) => ref ! ReportSnakesMsg(snakes(ref),(snakes - ref).values)
        }
        Logger.debug("waiting")
        context.become(waiting)
      }
    }

  }

  def gatherPlayers : Receive = {
    case players :Set[ActorRef] => {
      if(players.size > 0){
        requestedSnakes =  players
        snakes = scala.collection.mutable.Map.empty
        Logger.debug("gatherRequestSent to "+players.size+" players")
        context.become(calculateSnakes)
        players.foreach(player=>player ! GetSnakesMsg)
      }
      else{
        context.become(waiting)
      }
    }
  }


}