package game

import scala.util.Random

case class Point(x:Int,y:Int){
  def upOne(implicit space : Space) = (y-1) match {
    case newY if newY >= space.upBounds => copy(y=newY)
    case _ => copy(y=space.downBounds)
  }
  def downOne(implicit space : Space) = (y+1) match {
    case newY if newY <= space.downBounds => copy(y=newY)
    case _ => copy(y=space.upBounds)
  }
  def leftOne(implicit space : Space) = (x-1) match {
    case newX if newX >= space.leftBounds => copy(x= newX)
    case _ => copy(x = space.rightBounds)
  }
  def rightOne(implicit space : Space) = (x+1) match {
    case newX if newX <= space.rightBounds => copy(x = newX)
    case _ => copy(x = space.leftBounds)
  }
}

trait Direction
object Left extends Direction
object Right extends Direction
object Forwards extends Direction

//TODO should this really be a trait or something else
//the bounds are inclusive.
trait Space{
  def leftBounds : Int
  def rightBounds : Int
  def upBounds : Int
  def downBounds : Int

  def points : Set[Point] = {
    //TODO recursive implementation is difficult due to bounds being def
    var points : Set[Point] = Set.empty
    for(x <- leftBounds until rightBounds + 1){
      for(y <- upBounds until downBounds + 1){
        points = points + Point(x,y)
      }
    }
    return points
  }
}


case class Snake(points : List[Point],facing : Direction = Forwards, isAlive : Boolean = true, hasEaten : Boolean = false){

  val up = "Up"
  val down = "Down"
  val left = "Left"
  val right = "Right"

  def head = points.head
  def tail = points.tail

  def direction(implicit space : Space) = points match {
    case List(head,neck,_*) if head equals neck.rightOne => right
    case List(head,neck,_*) if head equals neck.leftOne => left
    case List(head,neck,_*) if head equals neck.upOne => up
    case List(head,neck,_*) if head equals neck.downOne =>down
  }

  private def newTail : List[Point] = {
    if(hasEaten) points
    else points.dropRight(1)
  }

  def tick(implicit space : Space):Snake = (direction,facing : Direction) match {
    case (`up`,Forwards) => this.copy(head.upOne :: newTail ,facing = Forwards, hasEaten=false)
    case (`up`,Left) => this.copy(head.leftOne :: newTail,facing =  Forwards, hasEaten=false)
    case (`up`,Right) => this.copy(head.rightOne ::newTail, facing = Forwards, hasEaten=false)
    case (`down`,Forwards) => this.copy(head.downOne :: newTail, facing = Forwards, hasEaten=false)
    case (`down`,Left) => this.copy(head.rightOne :: newTail, facing = Forwards, hasEaten=false)
    case (`down`,Right) => this.copy(head.leftOne :: newTail, facing = Forwards, hasEaten=false)
    case (`left`, Forwards) => this.copy(head.leftOne :: newTail, facing = Forwards, hasEaten=false)
    case (`left`, Right) => this.copy(head.upOne :: newTail, facing = Forwards, hasEaten=false)
    case (`left`, Left) => this.copy(head.downOne :: newTail, facing = Forwards, hasEaten=false)
    case (`right`, Forwards) => this.copy(head.rightOne :: newTail, facing = Forwards, hasEaten=false)
    case (`right`, Left) => this.copy(head.upOne :: newTail,  facing = Forwards, hasEaten=false)
    case (`right`, Right) => this.copy(head.downOne :: newTail,  facing = Forwards, hasEaten=false)
  }
}

//TODO this is pretty inefficient as it has to go through the snakes twice
//it must be possible to go through them once only
trait ProcessSnakes{

  def resolveCollisionsWithFood[T](snakes : Map[T, Snake], food : List[Point]) : (Map[T,Snake] , List[Point]) = {
    snakes.foldLeft(Map[T,Snake](),food){
      case ((snakesAcc,remainingFood), (id,snake)) => {
        if(snake.isAlive && remainingFood.contains(snake.head)) {
          (snakesAcc + (id->snake.copy(hasEaten = true)), remainingFood.filterNot(elem => elem == snake.head))
        }
        else (snakesAcc + (id->snake),remainingFood)

      }
    }

  }



  def generateNewFood(snakes : Iterable[Snake], food : List[Point], space : Space) : List[Point] = {
    val occupiedPoints : Set[Point] = snakes.flatMap{
      case snake if snake.isAlive => snake.points
      case _ => List.empty
    }.toSet ++ food
    val availableSpace = space.points &~ occupiedPoints
    if(availableSpace.isEmpty) food
    else food :+ Random.shuffle(availableSpace).head
  }

  def resolveCollisionsWithSnakes[T](snakes : Map[T,Snake]) : Map[T,Snake] =  {
    def isAlive(snake : Snake, otherSnakes : Iterable[Snake]) : Boolean= {
      if(!snake.isAlive) false
      else {
        !snake.tail.contains(snake.head) &&
        ! otherSnakes.exists(otherSnake => otherSnake.isAlive &&  otherSnake.points.contains(snake.head))
      }
    }
    snakes.map{
      case(id,snake)=>(id -> snake.copy(isAlive= isAlive(snake,(snakes - id).values)))
    }
  }
}

