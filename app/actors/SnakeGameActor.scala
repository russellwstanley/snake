package actors

import akka.actor.{Terminated, ActorRef, Actor}
import game.{Point, Snake, ProcessSnakes}
import scala.util.Random
import akka.routing.{Routee, BroadcastRoutingLogic, Router}

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
    //TODO could use a broadcast router here rather than going through all the players
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

