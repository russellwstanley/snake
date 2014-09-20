package controllers

import play.api.mvc._
import akka.actor._
import akka.pattern._
import play.api.Play.current
import play.api.libs.json.{JsArray, Json, JsValue}
import actors.{GamesListMsg, GetGamesMsg, Actors, PlayerActor, CreateGameMsg}
import java.util.concurrent.TimeUnit
import game.SnakeGame
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.Future

object Application extends Controller {

  implicit val timeout = akka.util.Timeout(3, TimeUnit.SECONDS)
  implicit val gameWrites = Json.writes[SnakeGame]

  val gameForm = Form(
    mapping(
      "name" -> text
    )(SnakeGame.apply)(SnakeGame.unapply)
  )

  def index = Action {
    Ok(views.html.index("Snake"))
  }

  def getGames = Action.async {
    def futureResponse = Actors.gameManagerActor ? GetGamesMsg()
    futureResponse.map {
      case GamesListMsg(games) => Ok(Json.toJson(games))
    }
  }

  def newGame = Action.async { implicit request => {

    gameForm.bindFromRequest.fold(
    formWithErrors => {
      Future(BadRequest("Form Binding Failed"))
    },
    game => {
      def futureResponse = Actors.gameManagerActor ? CreateGameMsg(game.name)
      futureResponse.map {
        case s : String => Ok(s)
      }
    }
    )
  }
  }

  def socket = WebSocket.acceptWithActor[String, JsValue] {
    request => out => Props(new PlayerActor(out))
  }
}

