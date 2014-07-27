package game

case class Point(x:Int,y:Int){
  def upOne = Point(x,y-1)
  def downOne = Point(x,y+1)
  def leftOne = Point(x-1,y)
  def rightOne = Point(x+1,y)
}

trait Direction
object Left extends Direction
object Right extends Direction
object Forwards extends Direction


case class Snake(points : List[Point],facing : Direction){

  val up = "Up"
  val down = "Down"
  val left = "Left"
  val right = "Right"


  def turn(direction : Direction) : Snake = {
    Snake(points,direction)
  }

  def head = points.head

  val direction = points match {
    case List(head,neck,_*) if head equals neck.rightOne => right
    case List(head,neck,_*) if head equals neck.leftOne => left
    case List(head,neck,_*) if head equals neck.upOne => up
    case List(head,neck,_*) if head equals neck.downOne =>down
  }

  def tick:Snake = (direction,facing : Direction) match {
    case (`up`,Forwards) => Snake(points.head.upOne :: points.dropRight(1),Forwards)
    case (`up`,Left) => Snake(points.head.leftOne :: points.dropRight(1),Forwards)
    case (`up`,Right) => Snake(points.head.rightOne :: points.dropRight(1),Forwards)
    case (`down`,Forwards) => Snake(points.head.downOne :: points.dropRight(1),Forwards)
    case (`down`,Left) => Snake(points.head.rightOne :: points.dropRight(1),Forwards)
    case (`down`,Right) => Snake(points.head.leftOne :: points.dropRight(1),Forwards)
    case (`left`, Forwards) => Snake(points.head.leftOne :: points.dropRight(1),Forwards)
    case (`left`, Right) => Snake(points.head.upOne :: points.dropRight(1),Forwards)
    case (`left`, Left) => Snake(points.head.downOne :: points.dropRight(1),Forwards)
    case (`right`, Forwards) => Snake(points.head.rightOne :: points.dropRight(1),Forwards)
    case (`right`, Left) => Snake(points.head.upOne :: points.dropRight(1) , Forwards)
    case (`right`, Right) => Snake(points.head.downOne :: points.dropRight(1) , Forwards)

  }
}

class SnakeGame {

  def addSnake(snake: Snake) = {
    snakes =  snakes :+ snake
  }

  var snakes : List[Snake] = List()
  def tick = {
    snakes = snakes.map(snake => snake.tick)
    snakes
  }
}
