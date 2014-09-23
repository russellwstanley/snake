package actors

import akka.actor.{Props, Terminated, ActorRef, Actor}
import akka.routing.{BroadcastRouter, Routee, Router, BroadcastRoutingLogic}

//class WatcherManager extends Actor{
//
//  var router = Router(BroadcastRoutingLogic(),Vector[Routee]())
//
//  def receive = {
//    case watchee : ActorRef => {
//      context watch watchee
//      router = router.addRoutee(watchee)
//    }
//    case Terminated(watchee) => router = router.removeRoutee(watchee)
//    case _ => router.route(_,sender)
//  }
//
//}

