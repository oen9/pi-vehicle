package oen.pi.vehicle.jvm.hardware

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import oen.pi.vehicle.jvm.config.Gpio
import oen.pi.vehicle.jvm.hardware.VehicleController._

class VehicleController[F[_] : Effect](stateRef: Ref[F, State], turningCS: ContextShift[IO], gpio: GpioController[F]) {

  val speedStep = 50
  val maxSpeed = 1024
  val minSpeed = 0

  def speedUp: F[Int] = setSpeed(_ + _)

  def speedDown: F[Int] = setSpeed(_ - _)

  def rideForward(): F[Unit] = for {
    _ <- stop()
    _ <- gpio.simpleRideForward()
  } yield ()

  def rideBackward(): F[Unit] = for {
    _ <- stop()
    _ <- gpio.simpleRideBackward()
  } yield ()

  def stop(): F[Unit] = for {
    _ <- stateRef.update(s => s.copy(turningStatus = TurningNone))
    _ <- gpio.simpleStop()
  } yield ()

  def shutdown(): F[Unit] = for {
    _ <- stop()
    _ <- gpio.simpleShutdown()
  } yield()

  def turnRight(): F[Unit] = for {
    _ <- stateRef.update(s => s.copy(turningStatus = TurningRight))
    _ <- scheduleTurningInAsync(TurningRight, gpio.simpleTurnRight())
  } yield ()

  def turnLeft(): F[Unit] = for {
    _ <- stateRef.update(s => s.copy(turningStatus = TurningLeft))
    _ <- scheduleTurningInAsync(TurningLeft, gpio.simpleTurnLeft())
  } yield ()

  private[this] def adjustSpeed(v: Int): Int = {
    if (v >= maxSpeed) maxSpeed
    else if (v <= minSpeed) minSpeed
    else v
  }

  private[this] def modifySpeed(state: State, op: (Int, Int) => Int): (State, Int) = {
    val rawNewSpeed = op(state.speed, speedStep)
    val adjusted = adjustSpeed(rawNewSpeed)
    (state.copy(speed = adjusted), adjusted)
  }

  private[this] def setSpeed(op: (Int, Int) => Int): F[Int] = for {
    newSpeed <- stateRef.modify(state => modifySpeed(state, op))
    _ <- gpio.setGpioSpeed(newSpeed)
  } yield newSpeed

  private[this] def scheduleTurningInAsync(tStatus: TurningStatus, simpleTurn: => F[Unit]): F[Unit] = {
    Effect[F].toIO({
      for {
        state <- stateRef.get
        _ <- if (state.turningStatus == tStatus) simpleTurn else Effect[F].unit
      } yield ()
    }).start(turningCS).to[F].map(_ => Unit)
  }
}

object VehicleController {
  def apply[F[_] : Effect](conf: Gpio, turningCS: ContextShift[IO]): F[VehicleController[F]] = for {
    stateRef <- Ref.of[F, State](State(conf.startSpeed))
    gpioController = {
      if (conf.isDummy) new DummyGpio()
      else new HwGpio(conf)
    }
  } yield new VehicleController[F](stateRef, turningCS, gpioController)

  sealed trait TurningStatus
  case object TurningLeft extends TurningStatus
  case object TurningRight extends TurningStatus
  case object TurningNone extends TurningStatus

  case class State(speed: Int, turningStatus: TurningStatus = TurningNone)
}
