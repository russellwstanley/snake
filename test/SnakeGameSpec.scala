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
  "Player" should {
    "push move should add a move to the move queue" in {
      Player().pushMove(Left) must equalTo(Player(List(Left)))
    }
    "pop with no move queue should return the player unchanged" in {
      Player().popMove must equalTo(Player(List.empty))
    }
    "pop with moves should return a new player with the move removed" in {
      Player().pushMove(Left).popMove must equalTo(Player(List.empty))
    }
    "getting move should return the move" in {
      Player().pushMove(Left).pushMove(Right).move must equalTo(Left)
    }
    "getting move with no move should return forwards" in {
      Player().move must equalTo(Forwards)
    }
  }
  "ProcessSnakes" should{
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

  "SnakeGame" should {
    "tick when empty with no food generated" in new SnakeEnv {
      trait NeverMakeFood extends FoodGeneration{
        override def isNewFood: Boolean = false
      }
      val game = new SnakeGame[String]("someid","aname") with NeverMakeFood
      game.tick must equalTo(game)
    }
    "tick when empty with food generated" in new SnakeEnv {
      trait AlwawsMakeNewFood extends FoodGeneration{
        override def isNewFood: Boolean = true
      }
      val game = new SnakeGame[String]("someid","aname") with AlwawsMakeNewFood
      game.tick.food.size must equalTo(1)
    }
    "add a new snake" in new SnakeEnv {
      val game = SnakeGame[String]("someid","aname") + "snakeId"
      game.snakes("snakeId") must not beNull
    }
    "apply moves" in new SnakeEnv {
      val game = SnakeGame[String]("someid","aname") + "snakeId"
      game.applyMoves(Map("snakeId"->Right)).snakes("snakeId").facing must equalTo(Right)

    }
    "resolve collisions with no snakes" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      val snakes : Map[String,AliveSnake] = Map()
      game.resolveCollisionsWithSnakes(snakes) must equalTo(snakes)
    }
    "one snake should stay alive" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val newSnakes = game.resolveCollisionsWithSnakes(Map("s1"->snake1))
      newSnakes("s1").isInstanceOf[AliveSnake] must beTrue
    }
    "resolve collisions with colliding snakes" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val snake2 = AliveSnake(List(Point(1,1),Point(1,2)))
      val newSnakes = game.resolveCollisionsWithSnakes(Map("s1"->snake1,"s2"->snake2))
      newSnakes("s1").isInstanceOf[AliveSnake] must beTrue
      newSnakes("s2").isInstanceOf[AliveSnake] must beFalse
    }
    "resolve collisions with yourself" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      //degenerate snake has collided with itself and collapsed to a point!
      val snake = AliveSnake(List(Point(0,0),Point(0,0)))
      val newSnakes = game.resolveCollisionsWithSnakes(Map("s1"->snake))
      newSnakes("s1").isInstanceOf[AliveSnake] must beFalse
    }
    "resolve head to head collisions" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val snake2 = AliveSnake(List(Point(0,1),Point(0,2)))
      val newSnakes = game.resolveCollisionsWithSnakes(Map("s1"->snake1,"s2"->snake2))
      newSnakes("s1").isInstanceOf[AliveSnake] must beFalse
      newSnakes("s2").isInstanceOf[AliveSnake] must beFalse
    }
    "resolve collisions with food (no food)" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val(newSnakes,newFood) = game.resolveCollisionsWithFood(Map("s1"->snake1), List.empty)
      newSnakes("s1").hasEaten must beFalse
    }
    "resolve collisions with food" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      val food = List(Point(0,1))
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val(newSnakes,newFood) = game.resolveCollisionsWithFood(Map("s1"->snake1), food)
      newFood must beEmpty
      newSnakes("s1").hasEaten must beTrue
    }
    "food should be generated in available space" in new SnakeEnv{
      implicit val smallSpace = new Space {
        override def downBounds: Int = 0
        override def upBounds: Int = 0
        override def rightBounds: Int = 2
        override def leftBounds: Int = 0
      }
      val snake1 = AliveSnake(List(Point(0,0),Point(1,0)))
      val game = SnakeGame[String]("someid","aname")
      val newFood = game.generateNewFood(List(snake1),List.empty)
      newFood must beEqualTo(List(Point(2,0)))

    }
    "eat food" in new SnakeEnv{

    }
    "feedsnakes with no food and no snakes should return an empty game" in new SnakeEnv{
      pending("TODO")
      //SnakeGame.feedSnakes(GameState[String](Map.empty,Nil)) must equalTo(GameState[String](Map.empty,Nil))
    }
    "feedsnakes with no food should return the same state" in new SnakeEnv{
      implicit val snakeSpace = space
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val state = GameState[String](Map("s1"->snake1),Set.empty)
      SnakeGame.feedSnakes(state) must equalTo(state)
    }
    "feedsnakes with food should feed snakes" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      val food = Set(Point(0,1))
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val initialState = GameState[String](Map("s1"->snake1),food)
      SnakeGame.feedSnakes(initialState).food must beEmpty
      SnakeGame.feedSnakes(initialState).snakes("s1").hasEaten must beTrue
    }
    "feed snakes with no food to be eaten should return the same state" in new SnakeEnv{
      implicit val snakeSpace = space
      val game = SnakeGame[String]("someid","aname")
      val food = Set(Point(2,3))
      val snake1 = AliveSnake(List(Point(0,1),Point(1,1)))
      val initialState = GameState[String](Map("s1"->snake1),food)
      SnakeGame.feedSnakes(initialState).food must equalTo(food)
      SnakeGame.feedSnakes(initialState).snakes("s1").hasEaten must beFalse
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
