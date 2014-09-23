/**
 * Created by russell on 13/07/14.
 */

import game._
import game.Point
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import scala.util.Random

class PointEnv extends Scope{

 val space = new Space{
        override def downBounds: Int = 10
        override def upBounds: Int = 0
        override def rightBounds: Int = 10
        override def leftBounds: Int = 0
      }
}

class SnakeEnv extends Scope with ProcessSnakes{

  val space = new Space {
    override def downBounds: Int = 10

    override def upBounds: Int = 0

    override def rightBounds: Int = 10

    override def leftBounds: Int = 0
  }

  def leftSnake(d: Direction): Snake = Snake(List(Point(0, 0), Point(1, 0)), d)

  def rightSnake(d: Direction): Snake = Snake(List(Point(1, 0), Point(0, 0)), d)

  def upSnake(d: Direction): Snake = Snake(List(Point(0, 0), Point(0, 1)), d)

  def downSnake(d: Direction): Snake = Snake(List(Point(0, 0), Point(0, -1)), d)
}

@RunWith(classOf[JUnitRunner])
class SnakeGameTest extends Specification {

  "Point" should {
    "wrap when they go over the board edge to the right" in new PointEnv{

      val point = new Point(space.rightBounds, 1)
      implicit val pointSpace = space
      point.rightOne must equalTo(Point(space.leftBounds, 1))
    }
    "wrap when they go over the board edge to the right when moved right twice" in new PointEnv{
      val point = new Point(space.rightBounds-1, 1)
      implicit val pointSpace = space
      point.rightOne.rightOne must equalTo(Point(space.leftBounds, 1))
    }
    "wrap when they go over the board edge to the left" in new PointEnv{
      val point = new Point(space.leftBounds, 1)
      implicit val pointSpace = space
      point.leftOne must equalTo(Point(space.rightBounds,1))

    }
    "wrap when they go over the board edge to the bottom" in new PointEnv{
      val point = new Point(1, space.downBounds)
      implicit val pointSpace = space
      point.downOne must equalTo(Point(1,space.upBounds))

    }
    "wrap when they go over the board edge to the top" in new PointEnv{
      val point = new Point(1, space.upBounds)
      implicit val pointSpace = space
      point.upOne must equalTo(Point(1,space.downBounds))

    }
  }
  "Snake" should {
    "tick when has been fed" in new SnakeEnv{
      implicit val snakeSpace = space
      val head = Point(0, 1)
      val tail = Point(0,0)
      val snake = new Snake(List(head, tail), facing=Forwards,hasEaten = true )
      snake.tick must equalTo(Snake(List(head.downOne,head,tail),facing=Forwards,hasEaten= false))

    }
    "tick with one right facing snake" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = rightSnake(Forwards)
      snake.tick must equalTo(Snake(List(snake.head.rightOne, snake.head), Forwards))
    }
    "tick with one right facing snake turning left" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = rightSnake(Left)
      snake.tick must equalTo(Snake(List(snake.head.upOne, snake.head), Forwards))
    }
    "tick with one right facing snake turning right" in new SnakeEnv {
      implicit val snakeSpace = space
      val snake = rightSnake(Right)
      snake.tick must equalTo(Snake(List(snake.head.downOne, snake.head), Forwards))
    }
    "tick with one left facing snake" in new SnakeEnv {
      implicit val snakeSpace = space
      val snake = leftSnake(Forwards)
      snake.tick must equalTo(Snake(List(snake.head.leftOne, snake.head), Forwards))
    }
    "tick with one left facing snake turning right" in new SnakeEnv {
      implicit val snakeSpace = space
      val snake = leftSnake(Right)
      snake.tick must equalTo(Snake(List(snake.head.upOne, snake.head), Forwards))
    }
    "tick with one left facing snake turning left" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = leftSnake(Left)
      snake.tick must equalTo(Snake(List(snake.head.downOne, snake.head), Forwards))
    }
    "tick with one down facing snake" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = downSnake(Forwards)
      snake.tick must equalTo(Snake(List(snake.head.downOne, snake.head), Forwards))
    }
    "tick with one down facing snake turning left" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = downSnake(Left)
      snake.tick must equalTo(Snake(List(snake.head.rightOne, snake.head), Forwards))
    }
    "tick with one down facing snake turning right" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = downSnake(Right)
      snake.tick must equalTo(Snake(List(snake.head.leftOne, snake.head), Forwards))
    }
    "tick with one up facing snake" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = upSnake(Forwards)
      snake.tick must equalTo(Snake(List(snake.head.upOne, snake.head), Forwards))
    }
    "tick with one up facing snake turning left" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = upSnake(Left)
      snake.tick must equalTo(Snake(List(snake.head.leftOne, snake.head), Forwards))
    }
    "tick with one up facing snake turning right" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = upSnake(Right)
      snake.tick must equalTo(Snake(List(snake.head.rightOne, snake.head), Forwards))
    }

  }
  "Player" should {
    "push move should add a move to the move queue" in new SnakeEnv{
      implicit val snakeSpace = space
      Player(List.empty,leftSnake(Forwards)).pushMove(Left) must equalTo(Player(List(Left),leftSnake(Forwards)))
    }
    "pop move with no move queue should return the player unchanged" in new SnakeEnv{
      implicit val snakeSpace = space
      Player(List.empty,leftSnake(Forwards)).popMove must equalTo(Player(List.empty,leftSnake(Forwards)))
    }
    "pop move with moves should return a new player with the move applied to the snake" in new SnakeEnv{
      implicit val snakeSpace = space
      val snake = leftSnake(Forwards)
      Player(List.empty,snake).pushMove(Left).popMove must equalTo(Player(List.empty,Snake(List(snake.head.downOne,snake.head),Forwards)))
    }
  }
  "ProcessSnakes" should{
    "resolve collisions with no snakes" in new SnakeEnv{
      implicit val snakeSpace = space
      val snakes : Map[String,Snake] = Map()
      resolveCollisionsWithSnakes(snakes) must equalTo(snakes)
    }
    "one snake should stay alive" in new SnakeEnv{
      implicit val snakeSpace = space
      val snake1 = Snake(List(Point(0,1),Point(1,1)))
      val newSnakes = resolveCollisionsWithSnakes(Map("s1"->snake1))
      newSnakes("s1").isAlive must beTrue
    }
    "one dead snake stays dead" in new SnakeEnv{
      implicit val snakeSpace = space
      val snake1 = Snake(List(Point(0,1),Point(1,1)),Forwards,false)
      val newSnakes = resolveCollisionsWithSnakes(Map("s1"->snake1))
      newSnakes("s1").isAlive must beFalse
    }
    "resolve collisions with colliding snakes" in new SnakeEnv{
      implicit val snakeSpace = space
      val snake1 = Snake(List(Point(0,1),Point(1,1)))
      val snake2 = Snake(List(Point(1,1),Point(1,2)))
      val newSnakes = resolveCollisionsWithSnakes(Map("s1"->snake1,"s2"->snake2))
      newSnakes("s1").isAlive must beTrue
      newSnakes("s2").isAlive must beFalse
    }
    "resolve collisions with yourself" in new SnakeEnv{
      implicit val snakeSpace = space
      //degenerate snake has collided with itself and collapsed to a point!
      val snake = Snake(List(Point(0,0),Point(0,0)))
      val newSnakes = resolveCollisionsWithSnakes(Map("s1"->snake))
      newSnakes("s1").isAlive must beFalse
    }
    "resolve head to head collisions" in new SnakeEnv{
      implicit val snakeSpace = space
      val snake1 = Snake(List(Point(0,1),Point(1,1)))
      val snake2 = Snake(List(Point(0,1),Point(0,2)))
      val newSnakes = resolveCollisionsWithSnakes(Map("s1"->snake1,"s2"->snake2))
      newSnakes("s1").isAlive must beFalse
      newSnakes("s2").isAlive must beFalse
    }
    "dead snakes stay dead" in new SnakeEnv{
      implicit val snakeSpace = space
      val snake1 = Snake(List(Point(0,1),Point(1,1)),Forwards,false)
      val snake2 = Snake(List(Point(1,1),Point(1,2)))
      val newSnakes = resolveCollisionsWithSnakes(Map("s1"->snake1,"s2"->snake2))
      newSnakes("s1").isAlive must beFalse
      newSnakes("s2").isAlive must beTrue

    }
    "resolve collisions with food (no food)" in new SnakeEnv{
      implicit val snakeSpace = space
      val snake1 = Snake(List(Point(0,1),Point(1,1)))
      val(newSnakes,newFood) = resolveCollisionsWithFood(Map("s1"->snake1), List.empty)
      newSnakes("s1").hasEaten must beFalse
    }
    "resolve collisions with food" in new SnakeEnv{
      implicit val snakeSpace = space
      val food = List(Point(0,1))
      val snake1 = Snake(List(Point(0,1),Point(1,1)))
      val(newSnakes,newFood) = resolveCollisionsWithFood(Map("s1"->snake1), food)
      newFood must beEmpty
      newSnakes("s1").hasEaten must beTrue
    }
    "dead snakes cannot eat food" in new SnakeEnv{
      implicit val snakeSpace = space
      val food = List(Point(0,1))
      val snake1 = Snake(List(Point(0,1),Point(1,1)),isAlive=false)
      val(newSnakes,newFood) = resolveCollisionsWithFood(Map("s1"->snake1), food)
      newFood must beEqualTo(food)
      newSnakes("s1").hasEaten must beFalse
    }
    "food should be generated in available space" in new SnakeEnv{
      val smallSpace = new Space {
        override def downBounds: Int = 0
        override def upBounds: Int = 0
        override def rightBounds: Int = 2
        override def leftBounds: Int = 0
      }
      val snake1 = Snake(List(Point(0,0),Point(1,0)))
      val newFood = generateNewFood(List(snake1),List.empty,smallSpace)
      newFood must beEqualTo(List(Point(2,0)))

    }
  }
  "Space" should {
    "Generate points correctly with empty space" in {
        val emptySpace = new Space {
          override def downBounds: Int = 0
          override def upBounds: Int = 0
          override def rightBounds: Int = 0
          override def leftBounds: Int = 0
        }
        emptySpace.points must equalTo(Set(Point(0,0)))


    }
    "Generate points correctly with non empty space" in {
      val nonEmptySpace = new Space {
        override def downBounds: Int = 1
        override def upBounds: Int = 0
        override def rightBounds: Int = 1
        override def leftBounds: Int = 0
      }
      nonEmptySpace.points must equalTo(Set(Point(0,0),Point(0,1),Point(1,0),Point(1,1)))


    }
  }


}
