package io.kodeasync.matches.service

import akka.actor.{Actor, ActorRef}
import io.kodeasync.matches.rest.client.ResponseModel.{JobDetail, Jobs, WorkerDetail, Workers}
import io.kodeasync.matches.service.MatchJobsServiceHandler.MatchJobsServiceHandlerModel.{FilterBasedOnSkills, _}
import io.kodeasync.matches.service.MatchJobsServiceHandler.MatchJobsServiceHandlerProtocol._
import io.kodeasync.matches.util.{DistanceFormula, Loggable}
import org.joda.time.DateTime

import scala.util.{Failure, Success}
import scala.concurrent.Future

/**
  * Created by shishir on 3/5/17.
  */
class MatchJobsServiceHandler extends Actor with Loggable {

  implicit val ec = context.dispatcher


  def receive = {
    case ReceiveJobsAndWorkers(workerId, workers, jobs) =>
      val replyTo = sender()
      workers.map { optWorkers =>
        optWorkers match {
          case Some(actualWorkers) => {
            val optWorkerDetail = actualWorkers.workers.filter(w => w.guid.get == workerId).headOption
            optWorkerDetail match {
              case Some(workerDetail) => self ! FilterJobsMatchingStartDate(workerDetail, jobs, replyTo)
              case None               => val e = MatchJobsServiceHandlerException(s"Worker with id = $workerId not found.")
                                          handleFailureResponse(replyTo, e)

            }
          }
          case None => {
            val e = MatchJobsServiceHandlerException(s"Worker with id = $workerId not found.")
            handleFailureResponse(replyTo,e)
          }
        }
      }

    case f: FilterJobsMatchingStartDate => {
      f.jobsFutureOpt onComplete{
        case Success(jobDataOp) =>
          jobDataOp.map{ jobData =>
            //compare all the jobs' start date with worker availability
            val jobsComparingAvailability = jobData.jobs.map { jobDetail =>
              val dateTime = new DateTime(jobDetail.startDate.get)
              val startDayOfTheWeek = dateTime.dayOfWeek().get()
              f.worker.availability match {
                case Some(availabilities) => {
                  val jobsMatchingStartDate = availabilities.flatten.filter { av =>
                    //non existent value 10 if dayIndex is not defined
                    av.dayIndex.getOrElse(10) == startDayOfTheWeek
                  }
                  Some(jobDetail)
                }
                case None => None
              }

            }
            val jobsMatchingAvailability = jobsComparingAvailability.flatten
            self ! FilterJobsMatchingLicenseRequirement(f.worker, jobsMatchingAvailability, f.replyTo)

          }.getOrElse{
            val e = MatchJobsServiceHandlerException(s"Jobs are not available.")
            handleFailureResponse(f.replyTo,e)
          }
        case Failure(e) =>
          handleFailureResponse(f.replyTo, e)
      }
    }

    case f: FilterJobsMatchingLicenseRequirement => {
      //check the license
      val resultJobs = f.worker.hasDriversLicense.getOrElse(false) match {
        case true  => {
          //check the transport mode
          f.worker.transportation match {
              case Some("CAR")  => {
                //get jobs that require license since the worker owns a CAR
                f.jobDetails.filter(job => job.driverLicenseRequired.getOrElse(false) == true)

              }
              case _  => {
                //get jobs not requiring license since worker has license but he doesn't own a CAR
                f.jobDetails.filter(job => job.driverLicenseRequired.getOrElse(false) == false)
              }
          }

        }
          //get jobs not requiring license
        case false => f.jobDetails.filter(job => job.driverLicenseRequired.getOrElse(false) == false)
      }

      self ! FilterJobsMatchingDistance(f.worker, resultJobs, f.replyTo)

    }

    case d: FilterJobsMatchingDistance => {
      //mapJobsWithDistance has some operations, so moved to a different method for clarity
      val jobsMappedToDistance = mapJobsWithDistance(d.worker, d.jobDetails)
      val workerMaxDistance = for {
        work <- Some(d.worker)
        searchAddr <- work.jobSearchAddress
        maxDistance <- searchAddr.maxJobDistance
      } yield maxDistance

      val jobsWithinDistance = for {
        job <- jobsMappedToDistance
      } yield {
        if(workerMaxDistance.get >= job._2) {
          Some(job._1)
        } else None
      }

      self ! FilterBasedOnCertificates(d.worker, jobsWithinDistance.flatten, d.replyTo)
    }

    case c: FilterBasedOnCertificates => {
      //We will compare required certificates in Jobs with respect to worker's certificates
      //Then score the jobs based on maximum matching number of certificates
      /*val workerCertItr = for {
        certsOpt <- c.worker.certificates
        certs <- certsOpt
      } yield certsOpt

      val workerCertificates = workerCertItr.toSet*/
      val workerCertificates = c.worker.certificates.getOrElse(Nil).toSet

      val jobCertItr = for {
        job <- c.jobDetails
        reqCertsOpt <- job.requiredCertificates
      } yield (job, reqCertsOpt)

      //score jobs
      val jobsWithScores = for {
        jobWithCerts <- jobCertItr
      } yield {
        val jobCertSet = jobWithCerts._2.toSet
        val matchingCerts = workerCertificates.intersect(jobCertSet)
        val matchCount = matchingCerts.size
        (jobWithCerts._1, matchCount)
      }

      self ! FilterBasedOnSkills(c.worker, jobsWithScores, c.replyTo)
    }

    case s: FilterBasedOnSkills => {
      //We will compare worker skills with respect to job title
      //Then score the jobs based on matching skills and having more other skills
      //Calculate (Job, maxNoOfMatchingCerts, skillToTitleMatch)
      /*val workerSkillItr = for {
        certsOpt <- s.worker.skills
        certs <- certsOpt
      } yield certs

      val workerSkillsSet = workerSkillItr.toSet*/
      val workerSkillsSet = s.worker.skills.getOrElse(Nil).toSet

      val jobsWithCertificateAndSkillMatch = for {
        job <- s.jobDetails
      } yield {
        val jobTitleSet = job._1.jobTitle.toSet
        val matchingSkillTitle = workerSkillsSet.intersect(jobTitleSet)
        val titleMatchCount = matchingSkillTitle.size
        ((job._1, titleMatchCount), job._2)
      }

      self ! ReturnOrderedJobs(jobsWithCertificateAndSkillMatch, s.replyTo)
    }

    case r: ReturnOrderedJobs => {
      val jobsWithCertificateSorted = r.jobWithCertsAndSkill.sortWith((l, r) => l._2 > r._2).map(f => (f._1))
      val finalJobsWithSkillSorted = jobsWithCertificateSorted.sortWith((l, r) => l._2 > r._2).map(f => (f._1))
      val finalJobs = finalJobsWithSkillSorted.length match {
        case size: Int if(size <= 3) => Jobs(finalJobsWithSkillSorted)
        case size: Int if(size > 3) => Jobs(finalJobsWithSkillSorted.take(3))
      }

      handleSuccessResponse(r.replyTo, JobsMatchResponse(finalJobs))
    }

    case MatchJobsServiceHandlerStop =>
      logger.debug(s"Stopping the MatchJobsServiceHandler")
      context.stop(self)

  }

  private def mapJobsWithDistance(worker: WorkerDetail, jobs: Seq[JobDetail]): Seq[(JobDetail, Double)] = {
    //map job with Location
    val jobWithLocation = for {
      job <- jobs
      loc <- job.location
      lat1 <- loc.latitude
      lon1 <- loc.longitude
    } yield (job, lat1.toDouble, lon1.toDouble)

    //map worker with location
    val workerWithLocation = for {
      loc <- worker.jobSearchAddress
      lat1 <- loc.latitude
      lon1 <- loc.longitude
    } yield (worker, lat1.toDouble, lon1.toDouble)

    //map jobs with distance from worker
    val jobLocationDistances = for {
      jl <- jobWithLocation
      wl <- workerWithLocation
    } yield (jl._1, DistanceFormula.getDistance(jl._2, wl._2, jl._3, wl._3))

    jobLocationDistances
  }

  private def handleSuccessResponse(replyTo: ActorRef, response: MatchJobsServiceHandlerResponse) = {
    logger.debug(s"The response is = ${response}")
    replyTo ! response
    self ! MatchJobsServiceHandlerStop
  }

  private def handleFailureResponse(replyTo: ActorRef, error: Throwable) = {
    logger.error(s"The error = $error")
    replyTo ! JobsMatchErrorResponse(error)
    self ! MatchJobsServiceHandlerStop
  }

}

object MatchJobsServiceHandler {

  case object MatchJobsServiceHandlerModel {

    case class FilterJobsMatchingStartDate(worker: WorkerDetail,
                                           jobsFutureOpt: Future[Option[Jobs]],
                                           replyTo: ActorRef)

    case class FilterJobsMatchingLicenseRequirement(worker: WorkerDetail,
                                                    jobDetails: Seq[JobDetail],
                                                    replyTo: ActorRef)

    case class FilterJobsMatchingDistance(worker: WorkerDetail,
                                          jobDetails: Seq[JobDetail],
                                          replyTo: ActorRef)

    case class FilterBasedOnCertificates(worker: WorkerDetail,
                                         jobDetails: Seq[JobDetail],
                                         replyTo: ActorRef)

    case class FilterBasedOnSkills(worker: WorkerDetail,
                                   jobDetails: Seq[(JobDetail, Int)],
                                   replyTo: ActorRef)

    case class ReturnOrderedJobs(jobWithCertsAndSkill: Seq[((JobDetail, Int), Int)],
                                 replyTo: ActorRef)

    case class MatchJobsServiceHandlerException(e: String) extends Exception

  }

  case object MatchJobsServiceHandlerProtocol {

    trait MatchJobsServiceHandlerRequest
    case class ReceiveJobsAndWorkers(workerId: String,
                                     workersFutureOpt: Future[Option[Workers]],
                                     jobsFutureOpt: Future[Option[Jobs]]) extends MatchJobsServiceHandlerRequest
    case object MatchJobsServiceHandlerStop

    trait MatchJobsServiceHandlerResponse
    case class JobsMatchResponse(matches: Jobs) extends MatchJobsServiceHandlerResponse
    case class JobsMatchErrorResponse(exception: Throwable) extends MatchJobsServiceHandlerResponse
  }

}
