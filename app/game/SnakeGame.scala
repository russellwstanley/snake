package game

import java.awt.Color

import scala.util.Random
import scala.annotation.tailrec

case class SnakeGame[T](id: String, name: String, state : GameState[T] = GameState[T]()) {

  implicit val space = new Space {

    def leftBounds: Int = 0

    def rightBounds: Int = 60

    def upBounds: Int = 0

    def downBounds: Int = 60
  }

  def next(moves : Map[T,Direction]) = copy(state=state.nextState(moves))
}


case class GameState[T](snakes: Map[T, Snake] = Map[T, Snake](), food: Set[Point] = Set[Point]()) {

  //FORTALK where to put the methods that transform gamestate

  val chanceOfNewFood = 0.02
  val minFood = 1
  val maxFood = 5
  lazy val aliveSnakes: Map[T, Snake] = snakes.filter(aliveSnakeFilter)
  lazy val deadSnakes: Map[T, Snake] = snakes.filterNot(aliveSnakeFilter)

  def aliveSnakeFilter(keypair: Tuple2[T, Snake]): Boolean = keypair match {
    case (_, AliveSnake(_, _, _)) => true
    case _ => false

  }

  val newSnake = AliveSnake(List(Point(0, 0), Point(1, 0))) //TODO need to generate a random snake that does not collide with others

  def +(id: T): GameState[T] = {
    copy(snakes = snakes + (id -> newSnake))
  }

  lazy val snakeHeads: Set[Point] = snakes.values.foldLeft(Set[Point]())((acc, snake) => snake match {
    case DeadSnake() => acc
    case AliveSnake(points, _, _) => acc + points.head
  })

  def mapSnakes(mapper : (Snake) => Snake) : GameState[T] = {
    this.copy(snakes = snakes.mapValues[Snake](mapper))
  }

  lazy val snakePoints: Iterable[Point] = snakes.values.flatMap(snake => snake.points)

  def feedSnakes : GameState[T] = {
    def feedSnake(snake: Snake): Snake = snake match {
      case s: DeadSnake => s
      case s: AliveSnake => if (food.contains(s.head)) s.copy(hasEaten = true) else s
    }

    val fedSnakes = snakes.mapValues[Snake](feedSnake)
    GameState(fedSnakes, food -- snakeHeads)
  }

  def killSnakes : GameState[T] = {
    def countHeadPoints(snake: AliveSnake) = snakePoints.count(p => p == snake.head)

    mapSnakes{
      case s: DeadSnake => s
      case s: AliveSnake => if (countHeadPoints(s) > 1) DeadSnake() else s
    }
  }

  def generateFood(implicit space: Space): GameState[T] = {
    val availableSpace = space.points &~ snakePoints.toSet
    if (availableSpace.isEmpty || ! isNewFood) this
    else {
      val newFood = Random.shuffle(availableSpace.toSeq).head
      copy(food = food + newFood)
    }
  }


  def isNewFood : Boolean = {
    if (food.size < minFood) true
    else if (food.size >= maxFood) false
    else Random.nextFloat() < chanceOfNewFood
  }

  def moveSnakes(moves : Map[T,Direction])(implicit space : Space) : GameState[T] = {
    val newSnakes = snakes.map {
      case (key, AliveSnake(points, facing, hasEaten)) => moves.get(key) match {
        case None => key -> AliveSnake(points, facing, hasEaten).tick
        case Some(direction) => key -> AliveSnake(points, direction, hasEaten).tick
      }
      case (key, s: DeadSnake) => key -> s

    }
    copy(snakes = newSnakes)
  }

  def nextState(moves : Map[T,Direction])(implicit space : Space) = {
    moveSnakes(moves).feedSnakes.killSnakes.generateFood
  }
}

case class Point(x: Int, y: Int) {
  def upOne(implicit space: Space) = (y - 1) match {
    case newY if newY >= space.upBounds => copy(y = newY)
    case _ => copy(y = space.downBounds)
  }

  def downOne(implicit space: Space) = (y + 1) match {
    case newY if newY <= space.downBounds => copy(y = newY)
    case _ => copy(y = space.upBounds)
  }

  def leftOne(implicit space: Space) = (x - 1) match {
    case newX if newX >= space.leftBounds => copy(x = newX)
    case _ => copy(x = space.rightBounds)
  }

  def rightOne(implicit space: Space) = (x + 1) match {
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
trait Space {
  def leftBounds: Int

  def rightBounds: Int

  def upBounds: Int

  def downBounds: Int

  def points: Set[Point] = {
    //TODO recursive implementation is difficult due to bounds being def
    var points: Set[Point] = Set.empty
    for (x <- leftBounds until rightBounds + 1) {
      for (y <- upBounds until downBounds + 1) {
        points = points + Point(x, y)
      }
    }
    return points
  }
}



//TODO color should be an actual color
case class Player(id : String, color : String) {
}

trait Snake {

  def points: List[Point]

  def facing: Direction

  def hasEaten: Boolean
}

case class DeadSnake() extends Snake {

  override def points = Nil

  override def facing = Forwards

  override def hasEaten = false

}

case class AliveSnake(points: List[Point], facing: Direction = Forwards, hasEaten: Boolean = false) extends Snake {

  val up = "Up"
  val down = "Down"
  val left = "Left"
  val right = "Right"

  def head = points.head

  def tail = points.tail

  def direction(implicit space: Space) = points match {
    case List(head, neck, _*) if head equals neck.rightOne => right
    case List(head, neck, _*) if head equals neck.leftOne => left
    case List(head, neck, _*) if head equals neck.upOne => up
    case List(head, neck, _*) if head equals neck.downOne => down
  }

  private def newTail: List[Point] = {
    if (hasEaten) points
    else points.dropRight(1)
  }

  def tick(implicit space: Space): AliveSnake = (direction, facing: Direction) match {
    case (`up`, Forwards) => this.copy(head.upOne :: newTail, facing = Forwards, hasEaten = false)
    case (`up`, Left) => this.copy(head.leftOne :: newTail, facing = Forwards, hasEaten = false)
    case (`up`, Right) => this.copy(head.rightOne :: newTail, facing = Forwards, hasEaten = false)
    case (`down`, Forwards) => this.copy(head.downOne :: newTail, facing = Forwards, hasEaten = false)
    case (`down`, Left) => this.copy(head.rightOne :: newTail, facing = Forwards, hasEaten = false)
    case (`down`, Right) => this.copy(head.leftOne :: newTail, facing = Forwards, hasEaten = false)
    case (`left`, Forwards) => this.copy(head.leftOne :: newTail, facing = Forwards, hasEaten = false)
    case (`left`, Right) => this.copy(head.upOne :: newTail, facing = Forwards, hasEaten = false)
    case (`left`, Left) => this.copy(head.downOne :: newTail, facing = Forwards, hasEaten = false)
    case (`right`, Forwards) => this.copy(head.rightOne :: newTail, facing = Forwards, hasEaten = false)
    case (`right`, Left) => this.copy(head.upOne :: newTail, facing = Forwards, hasEaten = false)
    case (`right`, Right) => this.copy(head.downOne :: newTail, facing = Forwards, hasEaten = false)
  }
}


