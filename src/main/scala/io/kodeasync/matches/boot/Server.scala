package io.kodeasync.matches.boot

import akka.http.scaladsl.Http
import io.kodeasync.matches.boot.Config.ServerConfig
import io.kodeasync.matches.rest.ApiModule
import io.kodeasync.matches.service.{DefaultSwipeJobsHttpComponent, SwipeJobsHttpComponent}
import io.kodeasync.matches.util.{AkkaServiceProvider, DefaultAkkaServiceProvider, Loggable}

/**
  * Created by shishir on 3/5/17.
  */
object Server extends App with ApplicationConfig with Loggable {

  val bindingFut = Http().bindAndHandle(routes, ServerConfig.interface, ServerConfig.port)

  bindingFut.onFailure {
    case e: Exception =>
      logger.error(s"Failed to bind $e")
  }

  println(s"Server online at http://localhost:8080. Press any key to stop...")
  //Console.in.read().toChar
  //bindingFut.flatMap(_.unbind()).onComplete(_ => system.terminate())

}

/**
  * ApplicationStack defines what modules the application requires.
  */
trait ApplicationStack extends ApiModule {
  this: SwipeJobsHttpComponent with AkkaServiceProvider =>
}

/**
  *  Configure concrete module implementations currently used by http server.
  */
trait ApplicationConfig extends ApplicationStack
  with DefaultSwipeJobsHttpComponent
  with DefaultAkkaServiceProvider



