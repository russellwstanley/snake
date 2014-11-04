package game

import scala.util.Random
import scala.annotation.tailrec

trait FoodGeneration{

  def isNewFood : Boolean
}


case class GameState[T](snakes : Map[T,Snake] = Map[T,Snake](), food : Set[Point] = Set[Point]()){

  def aliveSnakeFilter( keypair : Tuple2[ T , Snake]) : Boolean = keypair match {
    case (_,AliveSnake(_,_,_)) => true
    case _ => false

  }

  def ++ (other : GameState[T]) = GameState[T](snakes ++ other.snakes, food ++ other.food)

  val test = List(1,2,3).foldLeft(List[Int]())((b,a) => a :: b)

  def snakeHeads : Set[Point]= snakes.values.foldLeft(Set[Point]())((acc,snake)=>snake match{
    case DeadSnake() => acc
    case AliveSnake(points,_,_) => acc + points.head
  })


  val aliveSnakes : Map[T,Snake] = snakes.filter(aliveSnakeFilter)
  val deadSnakes : Map[T,Snake] = snakes.filterNot(aliveSnakeFilter)
}

object SnakeGame{


//  def feedSnakes[T](state : GameState[T]) : GameState[T] = {
//    def feedSnake(snake : Snake) : Snake = snake match {
//      case s : DeadSnake => s
//      case s : AliveSnake => if(state.food.contains(s.head)) s.copy(hasEaten = true) else s
//    }
//    val fedSnakes = state.snakes.mapValues[Snake](feedSnake)
//    GameState(fedSnakes, state.food -- state.snakeHeads)
//  }

  def feedSnake[T]( snake: Snake, food : Set[Point]) : (Snake,Set[Point]) = snake match{
    case s : DeadSnake => s->food
    case s : AliveSnake => {
      if(food.contains(s.head)) s.copy(hasEaten = true) -> (food - s.head)
      else s -> food
    }


  }

  def hasEaten(snake : Snake, food : Set[Point]) = snake match {
    case DeadSnake() => false
    case AliveSnake(points,_,_) => food.contains(points.head)
  }

  def eatenFood[T](snake : Snake,food : Set[Point]) : Option[Point] = snake match {
    case DeadSnake() => None
    case AliveSnake(points,_,_) => if(food.contains(points.head)) Some(points.head) else None
  }


  def feedSnakes[T](state : GameState[T]) : GameState[T] = {
    @tailrec
    def feedSnakesIter[T](state : GameState[T] , newState : GameState[T]) : GameState[T] = {
      if(state.snakes.isEmpty || state.food.isEmpty) state ++ newState
      else {
        val(key,snake) = state.snakes.head
        val (newSnake, newFood) = feedSnake(snake,state.food)
        feedSnakesIter(GameState[T](state.snakes.tail,newFood),GameState[T](newState.snakes + (key->newSnake),newFood))
      }
    }
    feedSnakesIter(state, GameState())
  }


//  val killSnakes = (state  :GameState[T])  =>  GameState[T]()
//  val generateFood = (state  :GameState[T])  =>  GameState[T]()
//
//  val next  =  feedSnakes andThen killSnakes andThen generateFood

}


case class SnakeGame[T](id : String, name : String, snakes : Map[T,Snake] = Map[T,AliveSnake](), food : List[Point] = Nil) extends  FoodGeneration {


      implicit val space = new Space  {
        def leftBounds : Int = 0
        def rightBounds : Int = 60
        def upBounds : Int = 0
        def downBounds : Int = 60
      }

      def resolveCollisionsWithFood[T](snakes : Map[T, AliveSnake], food : List[Point]) : (Map[T,AliveSnake] , List[Point]) = {
        snakes.foldLeft(Map[T,AliveSnake](),food){
          case ((snakesAcc,remainingFood), (id,snake)) => {
            if(remainingFood.contains(snake.head)) {
              (snakesAcc + (id->snake.copy(hasEaten = true)), remainingFood.filterNot(elem => elem == snake.head))
            }
            else (snakesAcc + (id->snake),remainingFood)

          }
        }

      }



      def generateNewFood(snakes : Iterable[Snake], food : List[Point])(implicit space : Space) : List[Point] = {
        val occupiedPoints : Set[Point] = snakes.flatMap{
          case AliveSnake(points,direction,hasEaten) => points
          case DeadSnake() => List.empty
        }.toSet ++ food
        val availableSpace = space.points &~ occupiedPoints
        if(availableSpace.isEmpty) food
        else food :+ Random.shuffle(availableSpace.toSeq).head
      }

      def resolveCollisionsWithSnakes[T](snakes : Map[T,AliveSnake]) : Map[T,Snake] =  {
        def isAlive(snake : AliveSnake, otherSnakes : Iterable[AliveSnake]) : Boolean= {
          !snake.tail.contains(snake.head) &&
            ! otherSnakes.exists(otherSnake => otherSnake.points.contains(snake.head))
        }
        snakes.map{
          case(id,snake)=>(id -> {
            if(isAlive(snake,(snakes - id).values)) snake
            else DeadSnake()
          })
        }
      }

      def applyMoves(moves : Map[T,Direction]) : SnakeGame[T] = {
        def movedSnakes = snakes.map{
          case(key,AliveSnake(points,facing,hasEaten)) => moves.get(key) match {
            case None => key->AliveSnake(points,facing,hasEaten)
            case Some(direction) => key->AliveSnake(points,direction,hasEaten)
          }
          case(key,s : DeadSnake) => key->s
        }
        copy(snakes = movedSnakes)
      }

      val newSnake = AliveSnake(List(Point(0,0),Point(1,0))) //TODO need to generate a random snake that does not collide with others


      def + (id : T) : SnakeGame[T] = {
        copy(snakes = snakes + (id -> newSnake))
      }

      def newFood(snakes : Iterable[AliveSnake], food : List[Point] ) : List[Point] = {
        if(isNewFood) return generateNewFood(snakes,food)
        else return food
      }

      def tick(): SnakeGame[T] = {
        //get new snakes after eating food and resolving collisions
        val aliveSnakes : Map[T,AliveSnake] = snakes.collect{
          case (key : T,s : AliveSnake) => (key,s)
        }
        val snakesAfterCollisions = snakes ++ resolveCollisionsWithSnakes(snakes.collect{
          case (key: T ,snake : AliveSnake) => (key,snake.tick)
        })
        val(fedSnakes,remainingFood) = resolveCollisionsWithFood(snakesAfterCollisions.collect{
          case(key : T, snake : AliveSnake)=>(key,snake)
        },food)

        //generate new food if necessary
        val generatedFood = newFood(fedSnakes.values,remainingFood)
        this.copy(snakes=snakesAfterCollisions ++ fedSnakes,food=generatedFood)
      }
      val chanceOfNewFood = 0.02
      val minFood = 1
      val maxFood = 5

      def isNewFood : Boolean = food.size match{
        case s if s < minFood => true
        case s if s >= maxFood => false
        case _ => Random.nextFloat() < chanceOfNewFood
      }


}


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

case class Player(moveQueue : List[Direction] = Nil){
  def pushMove(move : Direction) : Player = copy(moveQueue = moveQueue :+ move)
  def popMove : Player = moveQueue match {
    case Nil => copy()
    case head :: tail => copy(moveQueue = tail)
  }
  def move : Direction = moveQueue match {
    case Nil => Forwards
    case head :: tail => head
  }
}

trait Snake{

  def points : List[Point]
  def facing : Direction
  def hasEaten : Boolean
}

case class DeadSnake() extends Snake{

  override def points = Nil
  override def facing = Forwards
  override def hasEaten = false

}

//TODO should have class for deadsnake etc?
case class AliveSnake(points : List[Point],facing : Direction = Forwards, hasEaten : Boolean = false) extends Snake{

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

  def tick(implicit space : Space):AliveSnake = (direction,facing : Direction) match {
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


