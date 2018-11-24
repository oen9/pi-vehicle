package oen.pi.vehicle.jvm

import cats.effect.Effect
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import oen.pi.vehicle.jvm.hardware.VehicleController
import org.slf4j.LoggerFactory

class VehicleControlEndpoints[F[_] : Effect](vController: VehicleController[F]) extends Http4sDsl[F] {
  private[this] val logger = LoggerFactory.getLogger(getClass)

  def endpoints(): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "forward" =>
      for {
        _ <- vController.rideForward()
        _ <- Effect[F].delay(logger.info("↑"))
        response <- NoContent()
      } yield response
    case GET -> Root / "backward" =>
      for {
        _ <- vController.rideBackward()
        _ <- Effect[F].delay(logger.info("↓"))
        response <- NoContent()
      } yield response
    case GET -> Root / "left" =>
      for {
        _ <- vController.turnLeft()
        _ <- Effect[F].delay(logger.info("↰"))
        response <- NoContent()
      } yield response
    case GET -> Root / "right" =>
      for {
        _ <- vController.turnRight()
        _ <- Effect[F].delay(logger.info("↱"))
        response <- NoContent()
      } yield response
    case GET -> Root / "stop" =>
      for {
        _ <- vController.stop()
        _ <- Effect[F].delay(logger.info("⛔"))
        response <- NoContent()
      } yield response
    case GET -> Root / "speed-up" =>
      for {
        speed <- vController.speedUp
        _ <- Effect[F].delay(logger.info("increased speed: {}", speed))
        response <- NoContent()
      } yield response
    case GET -> Root / "speed-down" =>
      for {
        speed <- vController.speedDown
        _ <- Effect[F].delay(logger.info("decreased speed: {}", speed))
        response <- NoContent()
      } yield response
  }
}

object VehicleControlEndpoints {
  def apply[F[_] : Effect](vehicleController: VehicleController[F]): VehicleControlEndpoints[F] =
    new VehicleControlEndpoints[F](vehicleController)
}
