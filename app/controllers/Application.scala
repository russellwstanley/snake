package controllers

import play.api.mvc._
import akka.actor._
import akka.pattern._
import play.api.Play.current
import play.api.libs.json.{JsArray, Json, JsValue}
import actors._
import java.util.concurrent.TimeUnit
import game.SnakeGame
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.Future
import actors.CreateGameMsg

object Application extends Controller {

  case class GameData(name: String)

  val gameForm = Form(
    mapping(
      "name" -> text
    )(GameData.apply)(GameData.unapply)
  )

  implicit val timeout = akka.util.Timeout(3, TimeUnit.SECONDS)

  def index = Action {
    Ok(views.html.index("Snake"))
  }


  def newGame = Action {
    implicit request => {
      val gameData = gameForm.bindFromRequest.get
      Actors.gameManagerActor ! CreateGameMsg(gameData.name)
      Accepted("games")
    }
  }

  def games = Action{
    Ok(views.html.games())
  }

  def watchGames = WebSocket.acceptWithActor[String,JsValue]{
    request => out => Props(new GamesWatcherActor(out))

  }


  def socket = WebSocket.acceptWithActor[String, JsValue] {
    request => out => Props(new PlayerActor(out))
  }
}

