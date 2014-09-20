import org.junit.runner.RunWith
import org.specs2.runner._
import play.api.libs.json._
import play.api.test._
import game.SnakeGame
import play.Logger
import scala.concurrent._
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class SnakeWebSpec extends PlaySpecification{

  implicit val gameReads  = Json.reads[SnakeGame]

  "Snake game web app" should {
    "get games with no games created" in new WithApplication{
      val Some(result) = route(FakeRequest(GET,"/games"))
      contentAsJson(result) must equalTo(JsArray())
    }
    "get games with a game created" in new WithApplication{
      Await.result(route(FakeRequest(POST,"/games").withFormUrlEncodedBody(("name"->"test"))).get, 1 second)
      val Some(result) = route(FakeRequest(GET,"/games"))
      contentAsJson(result).validate[List[SnakeGame]].get must equalTo(List(SnakeGame("test")))
    }
  }

}
