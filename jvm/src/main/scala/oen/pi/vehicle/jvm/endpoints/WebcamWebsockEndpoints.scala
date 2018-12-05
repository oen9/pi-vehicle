package oen.pi.vehicle.jvm.endpoints

import cats.effect.{ConcurrentEffect, Effect, Timer}
import fs2.Sink
import oen.pi.vehicle.jvm.webcam.WebcamService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.slf4j.LoggerFactory

class WebcamWebsockEndpoints[F[_] : ConcurrentEffect : Timer](webcamService: WebcamService[F]) extends Http4sDsl[F] {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def endpoints(): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "ws" =>
      val toClient = webcamService.createFrameStream()
      val fromClient: Sink[F, WebSocketFrame] = _.evalMap(f => Effect[F].delay(logger.info(s"Rcv: $f")))
      WebSocketBuilder[F].build(toClient, fromClient)

    case GET -> Root / "on" =>
      Ok(webcamService.turnOnCam())

    case GET -> Root / "off" =>
      Ok(webcamService.turnOffCam())
  }
}

object WebcamWebsockEndpoints {
  def apply[F[_] : ConcurrentEffect : Timer](webcamService: WebcamService[F]): WebcamWebsockEndpoints[F] =
    new WebcamWebsockEndpoints[F](webcamService)
}
