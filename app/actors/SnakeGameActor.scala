package actors

import akka.actor.{ActorRef, Actor}
import game._
import scala.util.Random
import akka.routing.Routee
import game.Player
import game.Snake
import game.Point
import akka.actor.Terminated
import scala.Some

class SnakeGameActor extends  Actor with ProcessSnakes with GameSpace{

  var initialSnake = Snake(List(Point(0,0),Point(0,1),Point(0,2),Point(0,3)))
  var players : Map[ActorRef,Player] = Map.empty
  def snakes : Map[ActorRef,Snake] = {
    players.map{
      case (ref,player) => (ref,player.snake)
    }
  }
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
    val(newSnakes,remainingFood) = resolveCollisionsWithFood(resolveCollisionsWithSnakes(snakes),food)
    food = remainingFood
    if (isNewFood) food  = generateNewFood(newSnakes.values,food,space)
    //TODO could use a broadcast router here rather than going through all the players
    newSnakes.foreach{
      case(ref,newSnake) => {
        players = players + (ref -> players(ref).copy(snake=newSnake))
        ref ! ReportSnakesMsg(newSnakes(ref),(newSnakes - ref).values,food)
      }
    }
  }

  def receive = {
    case RegisterPlayerMsg => {
      context.watch(sender)
      players = players + (sender->Player(List.empty,initialSnake))
    }
    case TickMsg => {
      //move all players
      players = players.map{
        case (ref,player) => (ref,player.tick)
      }
      reportNextGameStateToPlayers
    }
    case move: Direction => players.get(sender) match {
      case Some(player) => players = players + (sender->player.pushMove(move))
    }

    case Terminated(actor) => players = players - actor
  }


}

