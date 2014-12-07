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
import play.api.Logger

import scala.util.Random

object Application extends Controller {

  val playerIdKey: String = "PLAYER_ID"
  case class GameData(name: String)

  val gameForm = Form(
    mapping(
      "name" -> nonEmptyText
    )(GameData.apply)(GameData.unapply)
  )

  implicit val timeout = akka.util.Timeout(3, TimeUnit.SECONDS)

  def index = Action {
    implicit request => {
      request.session.get(playerIdKey) match {
        case Some(id) => Ok(views.html.index("Snake"))
        case None => Ok(views.html.index("Snake")).withSession(playerIdKey-> generateNewId)
      }
    }
  }


  def generateNewId: String = {
    Random.nextString(24);
  }

  def newGame = Action {
    implicit request => {
      gameForm.bindFromRequest.fold(
        withErrors => BadRequest(views.html.createGameForm("Oops", withErrors)),
        gameData => {
          Actors.gameManagerActor ! CreateGameMsg(gameData.name)
          Redirect("games")
        }
      )
    }
  }

  def createGameForm = Action{
    Ok(views.html.createGameForm("Create a new game",gameForm))

  }

  def games = Action{
    Ok(views.html.games())
  }

  def watchGame(id:String) = WebSocket.acceptWithActor[String,JsValue]{
    Logger.debug("watchgame "+id)
    request => out => Props(new GameWatcherActor(id,out))
  }

  def playGame(gameId:String) = WebSocket.tryAcceptWithActor[String,JsValue]{

    request => {
      Future.successful(request.session.get(playerIdKey) match {
        case Some(playerId) => Right(out => Props(new PlayerActor(playerId,gameId, out)))
        case None => Left(Forbidden)
      })
    }

  }

  def game(id:String) = Action {
    Ok(views.html.game(id))
  }

  def watchGames = WebSocket.acceptWithActor[String,JsValue]{
    request => out => Props(new GamesWatcherActor(out))
  }
}

