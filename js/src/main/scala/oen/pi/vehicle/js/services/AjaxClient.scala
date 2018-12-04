package oen.pi.vehicle.js.services

import org.scalajs.dom

object AjaxClient {
  def vehicleRideForward(): Unit = dom.ext.Ajax.get("/vehicle/forward")
  def vehicleRideBackward(): Unit = dom.ext.Ajax.get("/vehicle/backward")
  def vehicleTurnLeft(): Unit = dom.ext.Ajax.get("/vehicle/left")
  def vehicleTurnRight(): Unit = dom.ext.Ajax.get("/vehicle/right")
  def vehicleStop(): Unit = dom.ext.Ajax.get("/vehicle/stop")
  def vehicleSpeedUp(): Unit = dom.ext.Ajax.get("/vehicle/speed-up")
  def vehicleSpeedDown(): Unit = dom.ext.Ajax.get("/vehicle/speed-down")

  def turnOnCam(): Unit = dom.ext.Ajax.get("/webcam/on")
  def turnOffCam(): Unit = dom.ext.Ajax.get("/webcam/off")
}
