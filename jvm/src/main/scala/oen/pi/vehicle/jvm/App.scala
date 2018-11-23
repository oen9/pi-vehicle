package oen.pi.vehicle.jvm

import java.util.concurrent.Executors

import cats.effect._
import cats.implicits._
import oen.pi.vehicle.jvm.config.AppConfig
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object App extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    createServer[IO]()
  }

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer](): F[ExitCode] = {
    for {
      conf <- AppConfig.read()
      blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
      staticEndpoints = StaticEndpoints[F](blockingEc)
      exitCode <- BlazeBuilder[F]
        .bindHttp(conf.http.port, conf.http.host)
        .mountService(staticEndpoints.endpoints(), "/")
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    } yield exitCode
  }
}
