package io.kodeasync.matches.rest

import akka.http.scaladsl.server.Route
import io.kodeasync.matches.rest.resources.MatchResources
import io.kodeasync.matches.service.SwipeJobsHttpComponent
import io.kodeasync.matches.util.AkkaServiceProvider

/**
  * Created by shishir on 3/5/17.
  */
trait ApiModule extends MatchResources {

  this: SwipeJobsHttpComponent with AkkaServiceProvider =>

  val routes: Route = {
    matchRoute
  }

}
