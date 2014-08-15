package controllers

import play.api.mvc._
import akka.actor._
import play.api.Play.current
import play.api.libs.json.JsValue
import actors.PlayerActor

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Snake"))
  }

  def socket = WebSocket.acceptWithActor[String,JsValue]{ request => out => Props(new PlayerActor(out))}
}

