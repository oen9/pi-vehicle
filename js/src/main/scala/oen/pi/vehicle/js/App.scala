package oen.pi.vehicle.js

import japgolly.scalajs.react.extra.router.{BaseUrl, Redirect, Router, RouterConfigDsl}
import japgolly.scalajs.react.vdom.html_<^._
import oen.pi.vehicle.js.modules.{About, Home, Layout}
import oen.pi.vehicle.js.services.AppCircuit
import org.scalajs.dom.html

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("App")
object App {

  sealed trait Loc
  case object HomeLoc extends Loc
  case object AboutLoc extends Loc

  @JSExport
  def main(target: html.Div): Unit = {

    val homeWrapper = AppCircuit.connect(_.clicks)

    val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
      import dsl._

      (emptyRule
        | staticRoute(root, HomeLoc) ~> render(homeWrapper(Home(_)))
        | staticRoute("#about", AboutLoc) ~> render(About())
        )
        .notFound(redirectToPage(HomeLoc)(Redirect.Replace))
        .setTitle(p => s"PAGE = $p | Example App")
    }.renderWith(Layout.apply)

    val router = Router(BaseUrl.until_#, routerConfig)
    router().renderIntoDOM(target)
  }
}
