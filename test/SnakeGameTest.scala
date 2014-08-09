/**
 * Created by russell on 13/07/14.
 */

import game._
import game.Point
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope

class TestEnv extends Scope {

  val game = new SnakeGame()

}

class PointEnv extends Scope{

 val space = new Space{
        override def downBounds: Int = 10
        override def upBounds: Int = 0
        override def rightBounds: Int = 10
        override def leftBounds: Int = 0
      }
}

@RunWith(classOf[JUnitRunner])
class SnakeGameTest extends Specification {

  "Snake Game" should {
    "tick with an empty board" in new TestEnv() {
      game.snakes must equalTo(game.tick)
    }
  }
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
    "respond to direction" in {
      val snake = new Snake(List(Point(0, 1), Point(0, 0)), Right)
      snake.facing must equalTo(Right)
      val newsnake = snake.turn(Left)
      newsnake.facing must be(Left)
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

  class SnakeEnv extends Scope {

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
}
