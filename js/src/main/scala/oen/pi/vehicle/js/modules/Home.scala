package oen.pi.vehicle.js.modules

import diode.react.ModelProxy
import oen.pi.vehicle.js.components.BlueButton
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.pi.vehicle.js.services.{Clicks, IncreaseClicks}
import oen.pi.vehicle.shared.HelloShared

object Home {

  case class Props(proxy: ModelProxy[Clicks])

  class Backend($: BackendScope[Props, Unit]) {
    def tick(): Callback = $.props.flatMap(_.proxy.dispatchCB(IncreaseClicks))

    def render(props: Props) =
      React.Fragment(
        <.div(^.cls := "content-head is-center",
          "Hello: " + HelloShared.TEST_STR
        ),
        <.div(^.cls := "content",
          <.div(^.cls := "l-box pure-g is-center",
            <.div(^.cls := "l-box pure-u-1 pure-u-md-1-2", BlueButton(BlueButton.Props("click me!", tick()))),
            <.div(^.cls := "l-box pure-u-1 pure-u-md-1-2", " clicks: " + props.proxy.value.count)
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("Home")
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[Clicks]) = component(Props(proxy))
}
