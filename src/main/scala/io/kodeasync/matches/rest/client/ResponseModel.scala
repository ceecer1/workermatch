package io.kodeasync.matches.rest.client

import java.util.Date

/**
  * Created by shishir on 3/5/17.
  */
object ResponseModel {

  //SwipeJobs response from Jobs, Workers test api
  trait SwipeJobsResponse

  // Generic response used to share model for error conditions
  trait SwipeJobsError extends SwipeJobsResponse

  //Valid Jobs response data model from SwipeJobs test API

  case class Jobs(jobs: Seq[JobDetail]) extends SwipeJobsResponse

  case class JobDetail(guid: String,
                       jobId: Option[Int],
                       driverLicenseRequired: Option[Boolean],
                       requiredCertificates: Option[Seq[String]],
                       location: Option[JobLocation], //assuming
                       billRate: Option[String],
                       workersRequired: Option[Int],
                       startDate: Option[Date],
                       about: Option[String],
                       jobTitle: Option[String],
                       company: Option[String])

  case class JobLocation(longitude: Option[String],
                         latitude: Option[String])

  case class Workers(workers: Seq[WorkerDetail]) extends SwipeJobsResponse

  case class WorkerDetail(guid: Option[String],
                          userId: Option[Int],
                          rating: Option[Int],
                          isActive: Option[Boolean],
                          certificates: Option[Seq[String]],
                          skills: Option[Seq[String]],
                          jobSearchAddress: Option[JobSearchAddress],
                          transportation: Option[String],
                          hasDriversLicense: Option[Boolean],
                          availability: Option[Seq[Option[Availability]]],
                          phone: Option[String],
                          email: Option[String],
                          name: Option[Name],
                          age: Option[Int])

  case class JobSearchAddress(unit: Option[String],
                              maxJobDistance: Option[Int],
                              longitude: Option[String],
                              latitude: Option[String])

  case class Availability(title: Option[String], dayIndex: Option[Int])

  case class Name(last: Option[String], first: Option[String])


  //Error details if provided in SwipeJobs test API response
  case class Error(code: Int,
                   message: String) extends SwipeJobsError

  //Exceptional API call conditions
  case class SwipeJobsGenericError(message: String) extends SwipeJobsError

  case class SwipeJobsErrorNotFound(message: String) extends SwipeJobsError

  case class SwipeJobsErrorTooManyRequests(message: String) extends SwipeJobsError

  case class SwipeJobsErrorForbidden(message: String) extends SwipeJobsError

}
