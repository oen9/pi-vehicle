package oen.pi.vehicle.js.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.pi.vehicle.js.components.BlueButton
import oen.pi.vehicle.js.services.{Clicks, IncreaseClicks}
import org.scalajs.dom.ext.KeyCode

object Home {

  case class Props(proxy: ModelProxy[Clicks])

  class Backend($: BackendScope[Props, Unit]) {
    def tick(): Callback = $.props.flatMap(_.proxy.dispatchCB(IncreaseClicks))

    def handleKey(e: ReactKeyboardEvent): Callback = CallbackOption.keyCodeSwitch(e) {
      case KeyCode.Up => tick()
      case KeyCode.Down => tick()
      case KeyCode.Left => tick()
      case KeyCode.Right => tick()
      case KeyCode.Space => tick()
      case KeyCode.A => tick()
      case KeyCode.Z => tick()
    }

    def render(props: Props) =
      React.Fragment(
        <.div(^.cls := "content", ^.onKeyDown ==> handleKey, ^.tabIndex := 0,
          <.div(^.cls := "l-box pure-g is-center",
            <.div(^.cls := "l-box pure-u-1-4"),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↑", tick()))),
            <.div(^.cls := "l-box pure-u-1-4"),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↑v", tick()))),
          ),
          <.div(^.cls := "l-box pure-g is-center",
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↰", tick()))),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("⛔", tick()))),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↱", tick()))),
            <.div(^.cls := "l-box pure-u-1-4", " clicks: " + props.proxy.value.count)
          ),
          <.div(^.cls := "l-box pure-g is-center",
            <.div(^.cls := "l-box pure-u-1-4"),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↓", tick()))),
            <.div(^.cls := "l-box pure-u-1-4"),
            <.div(^.cls := "l-box pure-u-1-4", BlueButton(BlueButton.Props("↓v", tick())))
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("Home")
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[Clicks]) = component(Props(proxy))
}
