package io.kodeasync.matches.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.kodeasync.matches.boot.Config.{ServerConfig, config}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

/**
  * Created by shishir on 3/5/17.
  */
trait AkkaServiceProvider {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val execContext: ExecutionContextExecutor
  implicit val timeout: Timeout

}

trait DefaultAkkaServiceProvider extends AkkaServiceProvider {
  override implicit val system: ActorSystem = ActorSystem(ServerConfig.systemName, config)
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val timeout: Timeout = Timeout(ServerConfig.defaultTimeout.seconds)
  override implicit val execContext: ExecutionContextExecutor = system.dispatcher
}
