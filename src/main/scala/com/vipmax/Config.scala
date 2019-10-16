package com.vipmax

object Config {
  val clusterName = "cluster"
  var protocol = "akka.tcp"

  def udpConf(port:Int)= {
    protocol = "akka"

    s"""akka {
       |  actor {
       |    provider = remote
       |    warn-about-java-serializer-usage = false
       |  }
       |  remote {
       |    artery {
       |      enabled = on
       |      transport = aeron-udp
       |      canonical.hostname = "127.0.0.1"
       |      canonical.port = $port
       |    }
       |  }
       |  watch-failure-detector {
       |    threshold = 20
       |    acceptable-heartbeat-pause = 120s
       |  }
       |}""".stripMargin

  }


  def tcpConf(port:Int)= s"""akka {
       |  actor {
       |    provider = remote
       |    warn-about-java-serializer-usage = false
       |  }
       |  remote {
       |    enabled-transports = ["akka.remote.netty.tcp"]
       |    netty.tcp {
       |      hostname = "127.0.0.1"
       |      port = $port
       |    }
       | }
       |}""".stripMargin


}
