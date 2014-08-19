import actors.PlayerActor
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.libs.Akka
import akka.pattern.ask
import akka.testkit.TestActorRef

/**
 * Created by russell on 19/08/14.
 */
@RunWith(classOf[JUnitRunner])
class ActorsTest extends Specification{

  def playersTestActor = {
    implicit val system = Akka.system
    TestActorRef[PlayerActor]
  }

  "PlayersActor" should {
    "Calculate differences between snakes correctly" in {
      pending("TODO")
    }
  }

}
