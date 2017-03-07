package io.kodeasync.matches.rest.resources

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.kodeasync.matches.service.{MatchJobsServiceHandler, SwipeJobsHttpComponent}
import io.kodeasync.matches.util.{AkkaServiceProvider, JsonSerialization, Loggable}
import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.pattern.ask
import io.kodeasync.matches.service.MatchJobsServiceHandler.MatchJobsServiceHandlerProtocol._
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by shishir on 3/5/17.
  */
trait MatchResources extends JsonSerialization with Loggable {

  this: SwipeJobsHttpComponent with AkkaServiceProvider =>

  lazy val getJobMatches = pathPrefix("matches") & get & path("workers"/Segment)

  lazy val getTestPath = pathPrefix("matches") & get & path("test")

  def matchRoute: Route = getJobMatches { workerId =>
    logger.info("Worker Id received " + workerId)
    val jobs = swipeJobsHttpService.getJobs()
    val workers = swipeJobsHttpService.getWorkers()
    val msg = ReceiveJobsAndWorkers(workerId, workers, jobs)
    completeMatchReqHandlerRequest(msg)
  } ~ getTestPath {
    complete(List(1, 2, 3, 4))
  }

  private def getDeviceReqHandler = system.actorOf((Props[MatchJobsServiceHandler]))

  private def completeMatchReqHandlerRequest(msg: MatchJobsServiceHandlerRequest)
                                            (implicit marshaller: MatchJobsServiceHandlerResponse => ToResponseMarshallable): Route = {

    onComplete(getMatchReqHandlerResponse(getDeviceReqHandler, msg)) {
      case Success(response) =>
        complete(response)
      case Failure(e) => complete(e)
    }

  }

  private def getMatchReqHandlerResponse(actorRef: ActorRef,
                                         msg: MatchJobsServiceHandlerRequest):Future[MatchJobsServiceHandlerResponse] = {
    (actorRef ? msg).mapTo[MatchJobsServiceHandlerResponse]
  }

}
