package oen.pi.vehicle.js.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object About {
  val component = ScalaComponent.builder[Unit]("About")
    .renderStatic(
      <.div(^.cls := "content-head is-center",
        <.p("About!")
      )
    )
    .build

  def apply() = component()
}
