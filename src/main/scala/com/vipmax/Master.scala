package com.vipmax

import akka.actor._
import com.typesafe.config.ConfigFactory.parseString
import com.vipmax.Config._
import com.vipmax.Messages.WorkerUp


object Master{
  def main(args: Array[String]): Unit = {
    println("Starting master")
    implicit val as = ActorSystem(clusterName, parseString(tcpConf(port = 2222)))
    val master = new Master()
    master.start()
  }
}

class Master {
  var actor: Option[ActorRef] = _

  def start()(implicit as:ActorSystem) {
    actor = Option(as.actorOf(Props[MasterActor], "master"))
  }

  def stop()(implicit as:ActorSystem) {
    actor.foreach(_ ! PoisonPill)
    as.terminate()
  }
}


class MasterActor extends Actor {
  override def receive: Receive = {

    case w:WorkerUp =>
      println(s"[Master] Worker up! $sender}")
      context.watch(sender)

    case Terminated(ref) =>
      println(s"[Master] Worker down? $ref")
      context.unwatch(ref)

    case m =>
      println(s"[Master] Unknown message=$m")

  }
}
