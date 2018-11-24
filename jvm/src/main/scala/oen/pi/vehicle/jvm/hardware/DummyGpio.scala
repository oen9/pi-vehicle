package oen.pi.vehicle.jvm.hardware

import cats.effect.Effect

class DummyGpio[F[_] : Effect]() extends GpioController[F] {

  def simpleShutdown(): F[Unit] = Effect[F].delay(println("gpio.shutdown()"))

  def simpleRideForward(): F[Unit] = Effect[F].delay(println("Brum brum forward!"))

  def simpleRideBackward(): F[Unit] = Effect[F].delay(println("Brum brum backward!"))

  def simpleStop(): F[Unit] = Effect[F].delay(println("motor stopped"))

  def setGpioSpeed(newSpeed: Int): F[Unit] = Effect[F].unit

  def simpleTurnRight(): F[Unit] = Effect[F].delay({
    println("Turning right!")
    Thread.sleep(1000)
  })

  def simpleTurnLeft(): F[Unit] = Effect[F].delay({
    println("Turning left!")
    Thread.sleep(1000)
  })
}
