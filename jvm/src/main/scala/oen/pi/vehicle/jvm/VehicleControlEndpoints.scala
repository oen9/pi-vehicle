package oen.pi.vehicle.jvm

import cats.effect.Effect
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.implicits._

class VehicleControlEndpoints[F[_] : Effect] extends Http4sDsl[F] {
  def endpoints(): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "forward" =>
      for {
        _ <- Effect[F].delay(println("forward!"))
        response <- NoContent()
      } yield response
    case GET -> Root / "backward" =>
      for {
        _ <- Effect[F].delay(println("backward!"))
        response <- NoContent()
      } yield response
    case GET -> Root / "left" =>
      for {
        _ <- Effect[F].delay(println("left!"))
        response <- NoContent()
      } yield response
    case GET -> Root / "right" =>
      for {
        _ <- Effect[F].delay(println("right!"))
        response <- NoContent()
      } yield response
    case GET -> Root / "stop" =>
      for {
        _ <- Effect[F].delay(println("STOP!"))
        response <- NoContent()
      } yield response
    case GET -> Root / "speed-up" =>
      for {
        _ <- Effect[F].delay(println("speed up!"))
        response <- NoContent()
      } yield response
    case GET -> Root / "speed-down" =>
      for {
        _ <- Effect[F].delay(println("speed down!"))
        response <- NoContent()
      } yield response
  }
}

object VehicleControlEndpoints {
  def apply[F[_] : Effect](): VehicleControlEndpoints[F] =
    new VehicleControlEndpoints[F]()
}