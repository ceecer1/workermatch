package io.kodeasync.matches.boot

import com.typesafe.config.ConfigFactory

/**
  * Created by shishir on 3/5/17.
  */
object Config {

  val config = ConfigFactory.load()

  case object ServerConfig {
    private val serverConfig = config.getConfig("server")
    lazy val systemName = serverConfig.getString("system-name")
    lazy val port = serverConfig.getInt("port")
    lazy val interface = serverConfig.getString("interface")
    lazy val defaultTimeout = serverConfig.getInt("request-timeout")
  }

  case object SwipeJobsConfig {
    private val sjConfig = config.getConfig("swipejobs-resources")
    lazy val jobs = sjConfig.getString("jobsResource")
    lazy val workers = sjConfig.getString("workersResource")
  }

}
