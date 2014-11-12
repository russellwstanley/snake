import actors.{GamesListMsg,GetGamesMsg, CreateGameMsg, GameManagerActor}
import akka.testkit.TestActorRef
import game.SnakeGame
import java.util.concurrent.TimeUnit
import org.specs2.mutable.Specification
import play.api.libs.concurrent.Akka
import akka.pattern._
import play.api.test._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ActorsSpec extends Specification{

  "game manager actor " should {
    "generate a new game" in new WithApplication{
      implicit val system = Akka.system
      implicit val timeout = akka.util.Timeout(2,TimeUnit.SECONDS)
      val manager = TestActorRef[GameManagerActor]
      manager.underlyingActor.receive(CreateGameMsg("test"))
      manager.underlyingActor.games.size must beEqualTo(1)
      manager.underlyingActor.receive(CreateGameMsg("test"))
      manager.underlyingActor.games.size must beEqualTo(2)
    }
    "get games" in new WithApplication{
      implicit val system = Akka.system
      implicit val timeout = akka.util.Timeout(2,TimeUnit.SECONDS)
      val manager = TestActorRef[GameManagerActor]
      val games = Await.result(manager ? GetGamesMsg(),Duration(4,TimeUnit.SECONDS)).asInstanceOf[GamesListMsg]
      pending("TODO")

    }
  }

}
