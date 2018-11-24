package oen.pi.vehicle.jvm.hardware

trait GpioController[F[_]] {

  def simpleShutdown(): F[Unit]

  def simpleTurnRight(): F[Unit]

  def simpleTurnLeft(): F[Unit]

  def simpleRideForward(): F[Unit]

  def simpleRideBackward(): F[Unit]

  def setGpioSpeed(newSpeed: Int): F[Unit]

  def simpleStop(): F[Unit]
}


