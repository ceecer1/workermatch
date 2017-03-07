/*

package io.kodeasync.matches.util

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.{RequestContext, RequestContextImpl, RouteResult}
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer
import akka.util.ByteString
import org.specs2.specification.core.{Fragments, SpecificationStructure}
import org.specs2.specification.create.FragmentsFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RouteTestSpecs2 extends RequestBuilding with SpecificationStructure with FragmentsFactory {

  implicit val system = ActorSystem("Main")

  implicit val materializer = ActorMaterializer()

  override def map(fs: => Fragments) =
    super
      .map(fs)
      .append({ println("hahaha"); fragmentFactory.step(system.terminate())} )

  implicit class HttpRequestWithSendTo(req: HttpRequest) {
    def context = new RequestContextImpl(req, null: LoggingAdapter, null: RoutingSettings)
    def ~>[A](f: RequestContext => A) = f(context)
  }

  def checkEntity(res: Future[RouteResult]): Future[String] =
    res.flatMap(
      _.asInstanceOf[RouteResult.Complete]
        .response
        .entity
        .dataBytes
        .runFold(ByteString())(_ ++ _).map(_.utf8String)
    )
}

*/
