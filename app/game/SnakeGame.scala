package game

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

trait Space{
  def leftBounds : Int
  def rightBounds : Int
  def upBounds : Int
  def downBounds : Int
}


case class Snake(points : List[Point],facing : Direction){

  val up = "Up"
  val down = "Down"
  val left = "Left"
  val right = "Right"


  def turn(direction : Direction) : Snake = {
    Snake(points,direction)
  }

  def head = points.head

  def direction(implicit space : Space) = points match {
    case List(head,neck,_*) if head equals neck.rightOne => right
    case List(head,neck,_*) if head equals neck.leftOne => left
    case List(head,neck,_*) if head equals neck.upOne => up
    case List(head,neck,_*) if head equals neck.downOne =>down
  }

  def tick(implicit space : Space):Snake = (direction,facing : Direction) match {
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

  implicit val space = new Space  {
    def leftBounds : Int = 0
    def rightBounds : Int = 100
    def upBounds : Int = 0
    def downBounds : Int = 100
  }

  def addSnake(snake: Snake) = {
    snakes =  snakes :+ snake
  }

  var snakes : List[Snake] = List()
  def tick = {
    snakes = snakes.map(snake => snake.tick)
    snakes
  }
}
