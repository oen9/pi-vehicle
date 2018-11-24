package oen.pi.vehicle.js.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.pi.vehicle.js.components.BlueButton
import oen.pi.vehicle.js.services.{AjaxClient, Clicks, IncreaseClicks}
import org.scalajs.dom.ext.KeyCode

object Home {

  case class Props(proxy: ModelProxy[Clicks])

  class Backend($: BackendScope[Props, Unit]) {
    def tick(): Callback = $.props.flatMap(_.proxy.dispatchCB(IncreaseClicks))

    def forward(): Callback = Callback(AjaxClient.vehicleRideForward()) >> tick()
    def backward(): Callback = Callback(AjaxClient.vehicleRideBackward()) >> tick()
    def left(): Callback = Callback(AjaxClient.vehicleTurnLeft()) >> tick()
    def right(): Callback = Callback(AjaxClient.vehicleTurnRight()) >> tick()
    def stop(): Callback = Callback(AjaxClient.vehicleStop()) >> tick()
    def speedUp(): Callback = Callback(AjaxClient.vehicleSpeedUp()) >> tick()
    def speedDown(): Callback = Callback(AjaxClient.vehicleSpeedDown()) >> tick()

    def handleKey(e: ReactKeyboardEvent): Callback = CallbackOption.keyCodeSwitch(e) {
      case KeyCode.Up => forward()
      case KeyCode.Down => backward()
      case KeyCode.Left => left()
      case KeyCode.Right => right()
      case KeyCode.Space => stop()
      case KeyCode.A => speedUp()
      case KeyCode.Z => speedDown()
    }

    def render(props: Props) =
      React.Fragment(
        <.div(^.cls := "content", ^.onKeyDown ==> handleKey, ^.tabIndex := 0,
          <.div(^.cls := "l-box pure-g is-center",
            <.div(^.cls := "l-box pure-u-1-4"),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↑", forward()))),
            <.div(^.cls := "l-box pure-u-1-4"),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↑v", speedUp()))),
          ),
          <.div(^.cls := "l-box pure-g is-center",
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↰", left()))),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("⛔", stop()))),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↱", right()))),
            <.div(^.cls := "l-box pure-u-1-4", " clicks: " + props.proxy.value.count)
          ),
          <.div(^.cls := "l-box pure-g is-center",
            <.div(^.cls := "l-box pure-u-1-4"),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↓", backward()))),
            <.div(^.cls := "l-box pure-u-1-4"),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↓v", speedDown())))
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("Home")
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[Clicks]) = component(Props(proxy))
}
