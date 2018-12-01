package oen.pi.vehicle.jvm.endpoints

import cats.effect.{Effect, Timer}
import fs2.Sink
import oen.pi.vehicle.jvm.webcam.FakeCamUtils
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.slf4j.LoggerFactory

import scala.concurrent.duration.DurationLong

class WebcamWebsockEndpoints[F[_] : Effect : Timer] extends Http4sDsl[F] {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def endpoints(): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "ws" =>
      val toClient = FakeCamUtils.createFakeCamStream(30.milliseconds)
      val fromClient: Sink[F, WebSocketFrame] = _.evalMap(f => Effect[F].delay(logger.info(s"Rcv: $f")))

      WebSocketBuilder[F].build(toClient, fromClient)
  }
}

object WebcamWebsockEndpoints {
  def apply[F[_] : Effect : Timer](): WebcamWebsockEndpoints[F] = new WebcamWebsockEndpoints[F]()
}
