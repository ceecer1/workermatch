This application is developed using Scala programming language and Akka-HTTP framework.


This matcher service is based on schema-based matching approach as long as a relation could be established and
assumptions of some real world scenarios.

Assumptions:

#1. `driverLicenseRequired` in a job means worker must have a driving license and transport mode has to be 'CAR'
#2. The job finishes on the same day after commencing on the start date
#3. There is no apparent link of job's billRate, about and company with respect to worker's rating, isActive, age. Thats
    why those schema properties have not been considered during job matching.

###################################################################################################
The matches REST resource that will take a workerId and return a list of no more than 3 matching jobs is exposed at
io.kodeasync.matches.rest.resources.MatchResources

The apiURL format
http://localhost:8080/matches/workers/:workerguid

e.g
http://localhost:8080/matches/workers/562f6647aa889f8c2676b0a4

###################################################################################################
Running the application

sbt run

###################################################################################################
io.kodeasync.matches.rest.client.SwipeJobsHttpClient accesses the SwipeJobs test REST api urls to get a list of jobs
and workers

###################################################################################################
io.kodeasync.matches.service.MatchJobsServiceHandler Actor does all the ground work on finding best job matches as
described in the following which is similar to the code flow inside this Actor:

ReceiveJobsAndWorkers -> This state receives the jobs and workers and they are passed into next subsequent step.

FilterJobsMatchingStartDate -> This state matches the worker's availability with the job startDate, this is strictly
                                checked. The obtained Jobs matching at this step is passed into the folowing step
                                sequentially.

FilterJobsMatchingLicenseRequirement -> This state filters out jobs based on workers license availability and transport
                                mode.

FilterJobsMatchingDistance -> This strictly checks the workers maximum distance with the job location. Then the
                                resulting jobs is passed into the following step.

FilterBasedOnCertificates -> This state checks the worker's certificate with the job's required certificates and provides
                            the scores based on the number of matching certificates.

FilterBasedOnSkills -> This state checks if the job title matches with any of the worker's skills and provides 0 or 1
                        point.

ReturnOrderedJobs -> At this stage, the resulting jobs are first sorted with max number of available certificates and
                    then finally sorted with positive skill matches.

###################################################################################################
The application development is simply carried out by just linking on the above steps.

To make a really good job matching engine, the work done on this project needs to be extended a lot.
Though a domain expert has accurate knowledge on how to improve business logics, here is my 2 cent.
If we could do proper machine learning as the data comes in from the worker's job search, assignment and
 performance behavior, we could get following relevant links in order to provide a better value of this service.

  #1. Worker may be interested to slightly cross the limit of maxJobDistance provided that the billRate is
        relatively more.
  #2. The higher the rating, the more a worker might get the billRate.
  #3. May be more ideas and rules might come from business users to make it better.

###################################################################################################
Technically, it requires several improvement as follows.
 #1. Though this application is not developed based on test driven development, testing needs
    to be covered. Provided some good time, I will be able to write up the test module.
 #2. To run this application in production, an organized and automated testing and deployment configurations needs to be setup.
    This application currently uses a single configuration setup for development.
 #3. The codes could be restructured into separate files/modules to make the readability better and improve the structure if needed.
 #4. Data driven development of the match filters need to be done.
 #5. More changes could be flagged by technical experts.
