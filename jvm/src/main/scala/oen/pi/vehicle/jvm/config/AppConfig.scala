package oen.pi.vehicle.jvm.config

import cats.effect.Sync
import cats.implicits._
import pureconfig.error.ConfigReaderFailures

class AppConfigException(failures: ConfigReaderFailures) extends RuntimeException(failures.toList.mkString(" "))

case class Http(port: Int, host: String)
case class StepMotor(pin01: Int, pin02: Int, pin03: Int, pin04: Int)
case class Gpio(pinForward: Int, pinBackward: Int, pinSpeed: Int, stepMotor: StepMotor, startSpeed: Int, isDummy: Boolean)
case class Webcam(isDummy: Boolean)
case class AppConfig(http: Http, gpio: Gpio, webcam: Webcam)

object AppConfig {
  def read[F[_] : Sync](): F[AppConfig] = {
    Sync[F].delay(pureconfig.loadConfig[AppConfig]).flatMap {
      case Right(conf) => Sync[F].pure(conf)
      case Left(e) => Sync[F].raiseError(new AppConfigException(e))
    }
  }
}
