package com.vipmax

object Messages {
  case class InitMasters(masters: Array[String])
  case class MasterUp(master:String)
  case class WorkerUp()
}
