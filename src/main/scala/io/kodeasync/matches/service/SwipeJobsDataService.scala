package io.kodeasync.matches.service

import java.security.MessageDigest

import io.kodeasync.matches.rest.client.SwipeJobsHttpClient
import io.kodeasync.matches.rest.client.ResponseModel._
import io.kodeasync.matches.util.{AkkaServiceProvider, Loggable}

import scala.concurrent.Future

/**
  * Created by shishir on 3/5/17.
  */
trait SwipeJobsHttpComponent {

  val swipeJobsHttpService: SwipeJobsHttpService

  trait SwipeJobsHttpService {
    def getJobs(): Future[Option[Jobs]]
    def getWorkers(): Future[Option[Workers]]
  }
}

trait DefaultSwipeJobsHttpComponent extends SwipeJobsHttpComponent with SwipeJobsHttpClient {

  this: AkkaServiceProvider =>

  override val swipeJobsHttpService: SwipeJobsHttpService = new DefaultSwipeJobsHttpService

  private class DefaultSwipeJobsHttpService extends SwipeJobsHttpService with Loggable {

    override def getJobs(): Future[Option[Jobs]] = {

      val fetchedJobs = fetchJobs().map { response =>
        val actualResponse = response match {
          case rep: Jobs =>
            Some(rep)
          case rep: SwipeJobsErrorNotFound =>
            logger.error(s"Jobs Not Found - ${rep.message}")
            None
          case rep: SwipeJobsErrorTooManyRequests =>
            logger.error(s"Too many requests - ${rep.message}")
            None
          case rep: SwipeJobsErrorForbidden =>
            logger.error(s"Access Forbidden - ${rep.message}")
            None
          case rep: SwipeJobsGenericError =>
            logger.error(s"Other Errors - ${rep.message}")
            None
        }
        actualResponse
      }
      fetchedJobs
    }

    override def getWorkers(): Future[Option[Workers]] = {

      val fetchedWorkers = fetchWorkers().map { response =>
        val actualResponse = response match {
          case rep: Workers =>
            Some(rep)
          case rep: SwipeJobsErrorNotFound =>
            logger.error(s"Workers Not Found - ${rep.message}")
            None
          case rep: SwipeJobsErrorTooManyRequests =>
            logger.error(s"Too many requests - ${rep.message}")
            None
          case rep: SwipeJobsErrorForbidden =>
            logger.error(s"Access Forbidden - ${rep.message}")
            None
          case rep: SwipeJobsGenericError =>
            logger.error(s"Other Errors - ${rep.message}")
            None
        }
        actualResponse
      }
      fetchedWorkers
    }



  }

}
