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

class SnakeEnv extends Scope{

  val space = new Space {
    override def downBounds: Int = 10

    override def upBounds: Int = 0

    override def rightBounds: Int = 10

    override def leftBounds: Int = 0
  }

  def leftSnake(d: Direction): AliveSnake = AliveSnake(List(Point(0, 0), Point(1, 0)), d)

  def rightSnake(d: Direction): AliveSnake = AliveSnake(List(Point(1, 0), Point(0, 0)), d)

  def upSnake(d: Direction): AliveSnake = AliveSnake(List(Point(0, 0), Point(0, 1)), d)

  def downSnake(d: Direction): AliveSnake = AliveSnake(List(Point(0, 0), Point(0, -1)), d)
}

@RunWith(classOf[JUnitRunner])
class SnakeGameSpec extends Specification {

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
      val snake = new AliveSnake(List(head, tail), facing=Forwards,hasEaten = true )
      snake.tick must equalTo(AliveSnake(List(head.downOne,head,tail),facing=Forwards,hasEaten= false))

    }
    "tick with one right facing snake" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = rightSnake(Forwards)
      snake.tick must equalTo(AliveSnake(List(snake.head.rightOne, snake.head), Forwards))
    }
    "tick with one right facing snake turning left" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = rightSnake(Left)
      snake.tick must equalTo(AliveSnake(List(snake.head.upOne, snake.head), Forwards))
    }
    "tick with one right facing snake turning right" in new SnakeEnv {
      implicit val snakeSpace = space
      val snake = rightSnake(Right)
      snake.tick must equalTo(AliveSnake(List(snake.head.downOne, snake.head), Forwards))
    }
    "tick with one left facing snake" in new SnakeEnv {
      implicit val snakeSpace = space
      val snake = leftSnake(Forwards)
      snake.tick must equalTo(AliveSnake(List(snake.head.leftOne, snake.head), Forwards))
    }
    "tick with one left facing snake turning right" in new SnakeEnv {
      implicit val snakeSpace = space
      val snake = leftSnake(Right)
      snake.tick must equalTo(AliveSnake(List(snake.head.upOne, snake.head), Forwards))
    }
    "tick with one left facing snake turning left" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = leftSnake(Left)
      snake.tick must equalTo(AliveSnake(List(snake.head.downOne, snake.head), Forwards))
    }
    "tick with one down facing snake" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = downSnake(Forwards)
      snake.tick must equalTo(AliveSnake(List(snake.head.downOne, snake.head), Forwards))
    }
    "tick with one down facing snake turning left" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = downSnake(Left)
      snake.tick must equalTo(AliveSnake(List(snake.head.rightOne, snake.head), Forwards))
    }
    "tick with one down facing snake turning right" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = downSnake(Right)
      snake.tick must equalTo(AliveSnake(List(snake.head.leftOne, snake.head), Forwards))
    }
    "tick with one up facing snake" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = upSnake(Forwards)
      snake.tick must equalTo(AliveSnake(List(snake.head.upOne, snake.head), Forwards))
    }
    "tick with one up facing snake turning left" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = upSnake(Left)
      snake.tick must equalTo(AliveSnake(List(snake.head.leftOne, snake.head), Forwards))
    }
    "tick with one up facing snake turning right" in new SnakeEnv() {
      implicit val snakeSpace = space
      val snake = upSnake(Right)
      snake.tick must equalTo(AliveSnake(List(snake.head.rightOne, snake.head), Forwards))
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

  "GameState" should {

    "feedsnakes with no food and no snakes should return an empty game" in new SnakeEnv{
      GameState[String]().feedSnakes must equalTo(GameState[String]())
    }
    "feedsnakes with no food should return the same state" in new SnakeEnv{
      implicit val snakeSpace = space
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val state = GameState[String](Map("s1"->snake1),Set.empty)
      state.feedSnakes must equalTo(state)
    }
    "feedsnakes with food should feed snakes" in new SnakeEnv{
      implicit val snakeSpace = space
      val food = Set(Point(0,1))
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val initialState = GameState[String](Map("s1"->snake1),food)
      initialState.feedSnakes.food must beEmpty
      initialState.feedSnakes.snakes("s1").hasEaten must beTrue
    }
    "feed snakes with no food to be eaten should return the same state" in new SnakeEnv{
      implicit val snakeSpace = space
      val food = Set(Point(2,3))
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val initialState = GameState[String](Map("s1"->snake1),food)
      initialState.feedSnakes.food must equalTo(food)
      initialState.feedSnakes.snakes("s1").hasEaten must beFalse
    }
    "kill snakes with no snakes should return the same state" in new SnakeEnv{
     GameState[String]().killSnakes must equalTo(GameState[String]())
    }
    "kill snakes with 1 snake that has not collided with itself should return the same state" in new SnakeEnv{
      implicit val snakeSpace = space
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val state = GameState[String](Map("s1"->snake1),Set.empty)
      state.killSnakes must equalTo(state)
    }
    "kill snakes with 1 snake that has collided with iteslt must return a state with a single dead snake" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      //degenerate snake has collided with itself and collapsed to a point!
      val snake = AliveSnake(List(Point(0,0),Point(0,0)))
      val state = GameState[String](Map("s1"->snake))
      state.killSnakes must equalTo(GameState(Map("s1"->DeadSnake())))
    }
    "head to head collisions should kill both snakes" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      val s1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val s2 = AliveSnake(List(Point(0,1),Point(0,2)))
      val state = GameState[String](Map("s1"->s1,"s2"->s2))
      state.killSnakes must equalTo(GameState(Map("s1"->DeadSnake(),"s2"->DeadSnake())))
    }


  }
  "GameState" should {
    "return liveSnakes when empty" in new SnakeEnv{
      val gameState = new GameState[String](Map.empty,Set())
      gameState.aliveSnakes must equalTo(Map.empty)
    }
    "return livesnakes when not empty" in new SnakeEnv{
      val gameState = new GameState[String](Map("alive"->AliveSnake(List())),Set.empty)
      gameState.aliveSnakes must equalTo(Map("alive"->AliveSnake(List())))
    }
    "return deadsnakes when empty" in new SnakeEnv{
      val gameState = new GameState[String](Map.empty,Set())
      gameState.deadSnakes must equalTo(Map.empty)

    }
    "return dead snakes when not empty" in new SnakeEnv{
      val gameState = new GameState[String](Map("dead"->DeadSnake()),Set.empty)
      gameState.deadSnakes must equalTo(Map("dead"->DeadSnake()))
    }
    "separate out alive and dead snakes" in new SnakeEnv{
      val gameState = new GameState[String](Map("alive"->AliveSnake(List.empty),"dead"->DeadSnake()),Set.empty)
      gameState.deadSnakes must equalTo(Map("dead"->DeadSnake()))
      gameState.aliveSnakes must equalTo(Map("alive"->AliveSnake(List())))


    }
  }


}
