package com.vipmax

import akka.actor._
import com.typesafe.config.ConfigFactory.parseString
import com.vipmax.Config._
import com.vipmax.Messages._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Worker{
  def main(args: Array[String]): Unit = {
    println("Starting worker")
    implicit val as = ActorSystem(clusterName, parseString(tcpConf(port = 0)))
    val worker = new Worker()
    worker.start()
    val masters = Array("127.0.0.1:2222", "127.0.0.1:2223")
    worker.connect(masters)
  }
}

class Worker {
  var actor: Option[ActorRef] = _

  def start()(implicit as:ActorSystem) {
    actor = Option(as.actorOf(Props[WorkerActor], "worker"))
  }

  def stop()(implicit as:ActorSystem) {
    actor.foreach(_ ! PoisonPill)
    as.terminate()
  }

  def connect(masters: Array[String]) {
    actor.foreach( _ ! InitMasters(masters))
  }
}

class WorkerActor extends Actor {
  var crawlerMasters = Array[String]()
  var activeMaster = ActorRef.noSender

  import context.dispatcher

  override def receive: Receive = {
    case InitMasters(masters) =>
      println(s"[Worker] InitializeMasters=" + masters.mkString)
      crawlerMasters = masters
      self ! MasterUp(masters.head)

    case MasterUp(master) =>
      println(s"[Worker] Connecting to the master=$master")
      val address = self.path.address
      context.actorSelection(masterPath(master))
        .resolveOne(10 seconds)
        .onComplete{
          case Success(masterRef) =>
            println("[Worker] Master is active")
            activeMaster = masterRef
            context.watch(masterRef)
            masterRef ! WorkerUp()

          case Failure(f) =>
            println("[Worker] Master is not active")
            activeMaster = ActorRef.noSender
            println(f.getMessage)
            context.system.scheduler.scheduleOnce(1 seconds,() => {
              val i = crawlerMasters.zipWithIndex.find(_._1 == master).get._2
              val m = if(crawlerMasters.size == 1 || i == crawlerMasters.size - 1)
                crawlerMasters.head else crawlerMasters(i + 1)
              self ! MasterUp(m)
            })
        }

    case Terminated(ref) =>
      println(s"[Worker] Master down? $ref")
      self ! MasterUp(crawlerMasters.head)

    case m =>
      println(s"[Worker] Unknown message=$m")

  }

  def masterPath(m:String) = s"${protocol}://$clusterName@$m/user/master"
}
