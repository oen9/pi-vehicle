package oen.pi.vehicle.jvm.hardware

import cats.effect.Effect
import com.pi4j.io.gpio.{GpioFactory, PinState, RaspiPin}
import oen.pi.vehicle.jvm.config.Gpio

class HwGpio[F[_] : Effect](val conf: Gpio) extends GpioController[F] {

  import HwGpio._

  private[this] val gpio = GpioFactory.getInstance()

  private[this] val motorForward = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(conf.pinForward), "forward", PinState.LOW)
  private[this] val motorBackward = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(conf.pinBackward), "backward", PinState.LOW)
  private[this] val speedController = gpio.provisionPwmOutputPin(RaspiPin.getPinByAddress(conf.pinSpeed), "speed", conf.startSpeed)

  private[this] val motorPins = List(
    gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(conf.stepMotor.pin01), "motor01", PinState.LOW),
    gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(conf.stepMotor.pin02), "motor02", PinState.LOW),
    gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(conf.stepMotor.pin03), "motor03", PinState.LOW),
    gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(conf.stepMotor.pin04), "motor04", PinState.LOW)
  )

  (motorPins ++ List(
    motorForward,
    motorBackward,
    speedController
  )).foreach(p => p.setShutdownOptions(true, PinState.LOW))

  def simpleStop(): F[Unit] = Effect[F].delay {
    motorForward.setState(PinState.LOW)
    motorBackward.setState(PinState.LOW)
  }

  def simpleShutdown(): F[Unit] = Effect[F].delay(gpio.shutdown())

  def simpleRideForward(): F[Unit] = Effect[F].delay(motorForward.setState(PinState.HIGH))

  def simpleRideBackward(): F[Unit] = Effect[F].delay(motorBackward.setState(PinState.HIGH))

  def setGpioSpeed(newSpeed: Int): F[Unit] = Effect[F].delay(speedController.setPwm(newSpeed))

  def simpleTurnRight(): F[Unit] = Effect[F].delay {
    (0 until stepCount).foreach { _ =>
      smSequencesReversed.foreach { smSeq =>
        smSeq.zip(motorPins).foreach(v => v._2.setState(v._1))
        Thread.sleep(2)
      }
    }
  }

  def simpleTurnLeft(): F[Unit] = Effect[F].delay {
    (0 until stepCount).foreach { _ =>
      smSequences.foreach { smSeq =>
        smSeq.zip(motorPins).foreach(v => v._2.setState(v._1))
        Thread.sleep(2)
      }
    }
  }
}

object HwGpio {
  val smSequences = List(
    List(PinState.HIGH, PinState.LOW, PinState.LOW, PinState.LOW),
    List(PinState.HIGH, PinState.HIGH, PinState.LOW, PinState.LOW),
    List(PinState.LOW, PinState.HIGH, PinState.LOW, PinState.LOW),
    List(PinState.LOW, PinState.HIGH, PinState.HIGH, PinState.LOW),
    List(PinState.LOW, PinState.LOW, PinState.HIGH, PinState.LOW),
    List(PinState.LOW, PinState.LOW, PinState.HIGH, PinState.HIGH),
    List(PinState.LOW, PinState.LOW, PinState.LOW, PinState.LOW),
    List(PinState.HIGH, PinState.LOW, PinState.LOW, PinState.HIGH)
  )

  val smSequencesReversed = smSequences.reverse
  val stepCount = 10
}
