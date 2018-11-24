package oen.pi.vehicle.jvm

import cats.effect.Effect
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class VehicleControlEndpoints[F[_] : Effect] extends Http4sDsl[F] {
  def endpoints(): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "up" => NoContent()
    case GET -> Root / "down" => NoContent()
    case GET -> Root / "left" => NoContent()
    case GET -> Root / "right" => NoContent()
    case GET -> Root / "stop" => NoContent()
    case GET -> Root / "speed-up" => NoContent()
    case GET -> Root / "speed-down" => NoContent()
  }
}

object VehicleControlEndpoints {
  def apply[F[_] : Effect](): VehicleControlEndpoints[F] =
    new VehicleControlEndpoints[F]()
}