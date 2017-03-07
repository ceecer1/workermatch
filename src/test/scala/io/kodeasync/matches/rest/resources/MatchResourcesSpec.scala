package io.kodeasync.server.rest.resources

import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.kodeasync.matches.rest.resources.MatchResources
import io.kodeasync.matches.service.DefaultSwipeJobsHttpComponent
import io.kodeasync.matches.util.{DefaultAkkaServiceProvider, JsonSerialization}
import org.scalatest.mockito.MockitoSugar


/**
  * Created by shishir on 3/7/17.
  */
class MatchResourcesSpec extends WordSpec with Matchers with ScalatestRouteTest with JsonSerialization
 with MatchResources with DefaultSwipeJobsHttpComponent with DefaultAkkaServiceProvider {

  /*val smallRoute =
    get {
      pathSingleSlash {
        complete {
          "Captain on the bridge!"
        }
      } ~
        path("ping") {
          complete(List(1, 2, 3))
        }
    }*/

  "The service" should {
    "return a List[Int] response for GET requests to /matches/test" in new Base {
      Get("/matches/test") ~> matchRoute ~> check {
        responseAs[List[Int]] shouldEqual List(1, 2, 3)
      }
    }

  }
}

trait Base/*(implicit actorSystem: ActorSystem)*/ extends MockitoSugar with DefaultSwipeJobsHttpComponent
  with DefaultAkkaServiceProvider with MatchResources {



  //override var swipeJobsHttpService: Base#SwipeJobsHttpService = _
  //val swipeJobsHttpService = mock[ActorLookup]
  /*val actorUnderTest = TestActorRef(new ResourceManagerResolver(actorReference))

  when(actorReference.actorLookup(customerId)) thenReturn customerManagerProbe.testActor
  when(actorReference.actorLookup(personaId)) thenReturn personaManagerProbe.testActor
  when(actorReference.actorLookup(appId)) thenReturn appManageProbe.testActor*/


}

object WorkerMatchTestConfig {
  def config = {

    ConfigFactory.parseString(
      s"""""".stripMargin)

  }


}
