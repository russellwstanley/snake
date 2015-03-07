import actors._
import akka.actor.{ActorSystem, Actor}
import akka.testkit.TestActorRef
import game._
import java.util.concurrent.TimeUnit
import org.specs2.mutable.Specification
import play.api.libs.concurrent.Akka
import akka.pattern._
import play.api.libs.json.{Json, JsValue}
import play.api.test._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ActorsSpec extends Specification {
  def getJsonSpy(implicit system: ActorSystem) = {
    TestActorRef(new Actor {
      var received: Option[JsValue] = None

      def receive = {
        case json: JsValue => received = Some(json)
      }
    })
  }

  "game manager actor " should {
    "generate a new game" in new WithApplication {
      implicit val system = Akka.system
      implicit val timeout = akka.util.Timeout(2, TimeUnit.SECONDS)
      val manager = TestActorRef[GameManagerActor]
      manager.underlyingActor.receive(CreateGameMsg("test"))
      manager.underlyingActor.games.size must beEqualTo(1)
      manager.underlyingActor.receive(CreateGameMsg("test"))
      manager.underlyingActor.games.size must beEqualTo(2)
    }
    "get games" in new WithApplication {
      implicit val system = Akka.system
      implicit val timeout = akka.util.Timeout(2, TimeUnit.SECONDS)
      val expected = List(GameHolder("test", null))
      val manager = TestActorRef[GameManagerActor]
      manager.underlyingActor.games = expected
      val returned = Await.result(manager ? GetGamesMsg, Duration(4, TimeUnit.SECONDS)).asInstanceOf[GamesListMsg]
      returned.games must equalTo(expected)


    }
  }
  "game info actor" should {
    "report whole state if new state detected" in new WithApplication {
      implicit val system = Akka.system
      val expected = Json.arr(
          Json.obj(
            "name" -> "player1",
            "length" -> 2,
            "color" -> "#000000")
        )

      val outputSpy = getJsonSpy
      val gameInfoActor = TestActorRef(new GameInfoActor("testid", outputSpy))
      val player = Player(id = "p1", name = "player1")
      val snake = AliveSnake(List(Point(0, 0), Point(0, 1)))
      val state = GameState[Player](snakes = Map(player -> snake))
      gameInfoActor ! ReportStateMsg(state)
      outputSpy.underlyingActor.received.get must equalTo(expected)
    }

    "not report state if same state is detected" in new WithApplication {
      implicit val system = Akka.system
      val outputSpy = getJsonSpy
      val gameInfoActor = TestActorRef(new GameInfoActor("testid", outputSpy))
      val player = Player(id = "p1", name = "player1")
      val snake = AliveSnake(List(Point(0, 0), Point(0, 1)))
      val state = GameState[Player](snakes = Map(player -> snake))

      gameInfoActor ! ReportStateMsg(state)
      outputSpy.underlyingActor.received = None

      gameInfoActor ! ReportStateMsg(state)
      outputSpy.underlyingActor.received must beNone

    }
    "not report state if only the direction changes" in new WithApplication {
      implicit val system = Akka.system
      val outputSpy = getJsonSpy
      val gameInfoActor = TestActorRef(new GameInfoActor("testid", outputSpy))
      val player = Player(id = "p1", name = "player1")
      val snake = AliveSnake(List(Point(0, 0), Point(0, 1)))
      val state = GameState[Player](snakes = Map(player -> snake))

      gameInfoActor ! ReportStateMsg(state)
      outputSpy.underlyingActor.received = None
      //reset the spy

      val newSnake = snake.copy(facing = Left)
      val newState = GameState[Player](snakes = Map(player -> newSnake))
      gameInfoActor ! ReportStateMsg(newState)
      outputSpy.underlyingActor.received must beNone
    }

  }


}

