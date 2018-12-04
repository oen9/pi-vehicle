package oen.pi.vehicle.jvm

import java.util.concurrent.Executors

import cats.effect._
import cats.implicits._
import com.github.sarxos.webcam.Webcam
import oen.pi.vehicle.jvm.config.AppConfig
import oen.pi.vehicle.jvm.endpoints.{StaticEndpoints, VehicleControlEndpoints, WebcamWebsockEndpoints}
import oen.pi.vehicle.jvm.hardware.VehicleController
import oen.pi.vehicle.jvm.webcam.WebcamService
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object App extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    createSingleThreadContextShift[IO].use { cs =>
      createCamResource[IO].use { webcam =>
        createServer[IO](cs, webcam)
      }
    }
  }

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer](turningCS: ContextShift[IO], webcam: Webcam): F[ExitCode] = {
    for {
      conf <- AppConfig.read()
      blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
      vehicleController <- VehicleController[F](conf.gpio, turningCS)
      webcamService <- WebcamService[F](webcam, blockingEc)
      staticEndpoints = StaticEndpoints[F](blockingEc)
      vehicleControlEndpoints = VehicleControlEndpoints[F](vehicleController)
      webcamWebsockEndpoints = WebcamWebsockEndpoints[F](webcamService)
      exitCode <- BlazeBuilder[F]
        .bindHttp(conf.http.port, conf.http.host)
        .mountService(staticEndpoints.endpoints(), "/")
        .mountService(vehicleControlEndpoints.endpoints(), "/vehicle")
        .mountService(webcamWebsockEndpoints.endpoints(), "/webcam")
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    } yield exitCode
  }

  def createSingleThreadContextShift[F[_] : Effect]: Resource[F, ContextShift[IO]] = {
    Resource[F, ContextShift[IO]](
      Effect[F].delay {
        val executor = Executors.newSingleThreadExecutor()
        val ec = ExecutionContext.fromExecutor(executor)
        val ioContextShift = IO.contextShift(ec)
        (ioContextShift, Effect[F].delay(executor.shutdown()))
      }
    )
  }

  def createCamResource[F[_] : Effect]: Resource[F, Webcam] = {
    Resource[F, Webcam](Effect[F].delay {
      val webcam = Webcam.getDefault()
      (webcam, Effect[F].delay(webcam.close()))
    })
  }
}
