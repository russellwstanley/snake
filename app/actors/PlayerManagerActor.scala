package actors

import akka.actor.Actor
import game.Player

import scala.util.Random

class PlayerManagerActor extends Actor {

  var id = 0;

  def receive = {
    case CreatePlayerMsg => {
      sender ! Player("pid_" + id.toString, "#%06X".format(Random.nextInt(16581375)))
      id = id + 1
    }
  }

}
