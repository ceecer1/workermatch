package io.kodeasync.matches.rest.client

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import io.kodeasync.matches.boot.Config.SwipeJobsConfig
import akka.util.ByteString
import scala.concurrent.Future
import scala.concurrent.duration._
import io.kodeasync.matches.util.{AkkaServiceProvider, JsonSerialization, Loggable}

/**
  * Created by shishir on 3/5/17.
  */
trait SwipeJobsHttpClient extends JsonSerialization with Loggable {

  this: AkkaServiceProvider =>

  import ResponseModel._

  def fetchJobs(): Future[SwipeJobsResponse] = {
    val jobsApiUrl = SwipeJobsConfig.jobs
    logger.info(s"Request URL : $jobsApiUrl")
    val httpRequest = HttpRequest(uri = jobsApiUrl)
    val httpResponse: Future[HttpResponse] = Http().singleRequest(httpRequest)
    logger.info("Executed http request ")

    httpResponse flatMap { response =>
      // toStrict to enforce all data be loaded into memory from the connection
      val strictEntity = response.entity.toStrict(5.seconds)
      strictEntity flatMap { e =>
        val responseStr = e.dataBytes
          .runFold(ByteString.empty) { case (acc, b) => acc ++ b }
        response.status match {
          case OK => responseStr map { s =>
            //logger.info(s.utf8String)
            val jobsList = serialization.read[Seq[JobDetail]](s.utf8String)
            Jobs(jobsList)
          }
          case Forbidden => responseStr.map(s => SwipeJobsErrorForbidden(s.utf8String))
          case TooManyRequests => responseStr.map(s => SwipeJobsErrorTooManyRequests(s.utf8String))
          case NotFound => responseStr.map(s => SwipeJobsErrorNotFound(s.utf8String))
          case _ => responseStr.map(s => SwipeJobsGenericError(s.utf8String))
        }
      }
    }
  }

  def fetchWorkers(): Future[SwipeJobsResponse] = {
    val workersApiUrl = SwipeJobsConfig.workers
    logger.info(s"Request URL : $workersApiUrl")
    val httpRequest = HttpRequest(uri = workersApiUrl)
    val httpResponse: Future[HttpResponse] = Http().singleRequest(httpRequest)
    logger.info("Executed http request ")

    httpResponse flatMap { response =>
      // toStrict to enforce all data be loaded into memory from the connection
      val strictEntity = response.entity.toStrict(5.seconds)
      strictEntity flatMap { e =>
        val responseStr = e.dataBytes
          .runFold(ByteString.empty) { case (acc, b) => acc ++ b }
        response.status match {
          case OK => responseStr map { s =>
            //logger.info(s.utf8String)
            val workersList = serialization.read[Seq[WorkerDetail]](s.utf8String)
            Workers(workersList)
          }
          case Forbidden => responseStr.map(s => SwipeJobsErrorForbidden(s.utf8String))
          case TooManyRequests => responseStr.map(s => SwipeJobsErrorTooManyRequests(s.utf8String))
          case NotFound => responseStr.map(s => SwipeJobsErrorNotFound(s.utf8String))
          case _ => responseStr.map(s => SwipeJobsGenericError(s.utf8String))
        }
      }
    }
  }

}

