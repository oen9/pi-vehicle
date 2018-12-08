package oen.pi.vehicle.js.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.pi.vehicle.js.components.BlueButton
import oen.pi.vehicle.js.services.{AjaxClient, Clicks}
import oen.pi.vehicle.shared.{VideoFrame, WsData}
import org.scalajs.dom
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}

import scala.scalajs.js

object Webcam {
  val protocol = if ("http:" == dom.window.location.protocol) "ws://" else "wss://"
  val url = protocol + dom.window.location.host + "/webcam/ws"

  case class Fps(value: Int = 0, lastCalculated: Double = dom.window.performance.now(), framesWithoutCalculation: Int = 0)

  case class State(ws: Option[WebSocket], videoFrame: Option[VideoFrame], reconnectId: Option[Int] = None, fps: Fps = Fps())

  case class Props(proxy: ModelProxy[Clicks])

  def turnOnCam() = Callback(AjaxClient.turnOnCam())

  def turnOffCam() = Callback(AjaxClient.turnOffCam())

  class Backend($: BackendScope[Props, State]) {
    def render(state: State, props: Props) = {
      <.div(^.cls := "content is-center",
        <.div(^.cls := "l-box pure-g is-center",
          <.div(^.cls := "l-box pure-u-1-2", BlueButton(BlueButton.Props("turn on", turnOnCam()))),
          <.div(^.cls := "l-box pure-u-1-2", BlueButton(BlueButton.Props("turn off", turnOffCam()))),
        ),
        state.videoFrame.fold(
          <.div("Loading webcam...")
        )(v =>
          <.div(<.img(^.src := s"${v.frameType}${v.frame}"))
        ),
        <.p(s"fps: ${state.fps.value}"),
        Home(props.proxy)
      )
    }

    def calculateFps(oldFps: Fps): Fps = {
      val fpsInterval = 10
      val fwc = oldFps.framesWithoutCalculation + 1

      if (fwc == fpsInterval) {
        val newLastCalculated = dom.window.performance.now
        val delta = (newLastCalculated - oldFps.lastCalculated) / 1000
        val fps = fpsInterval / delta
        Fps(fps.toInt, newLastCalculated, 0)
      } else
        oldFps.copy(framesWithoutCalculation = fwc)
    }

    def start: Callback = {

      def connect = CallbackTo[WebSocket] {
        val direct = $.withEffectsImpure

        def onopen(e: Event): Unit = {
          println("[ws] connected")
        }

        def onmessage(e: MessageEvent): Unit = {
          WsData.fromJson(e.data.toString) match {
            case v: VideoFrame =>
              val newFps = calculateFps(direct.state.fps)
              direct.modState(_.copy(videoFrame = Some(v), fps = newFps))
          }
        }

        def onerror(e: Event): Unit = {
          val msg: String = e.asInstanceOf[js.Dynamic]
            .message.asInstanceOf[js.UndefOr[String]]
            .fold(s"error occurred!")("error occurred: " + _)
          println(s"[ws] $msg")
        }

        def onclose(e: CloseEvent): Unit = {
          println(s"""[ws] closed. Reason = "${e.reason}"""")
          println("[ws] reconnecting...")
          val recId = dom.window.setTimeout(() => conn().runNow(), 5000)
          direct.modState(state => {
            state.reconnectId.foreach(dom.window.clearInterval)
            state.copy(videoFrame = None, reconnectId = Some(recId))
          })
        }

        val ws = new WebSocket(url)
        ws.onopen = onopen _
        ws.onclose = onclose _
        ws.onmessage = onmessage _
        ws.onerror = onerror _
        ws
      }

      def conn(): CallbackTo[Unit] = {
        connect.attempt.flatMap {
          case Right(ws) => Callback(println(s"[ws] connecting to $url")) >> $.modState(_.copy(ws = Some(ws)))
          case Left(error) => Callback(println(s"[ws] error connecting: ${error.getMessage}"))
        }
      }

      conn()
    }

    def end: Callback = {
      def clearReconnecting = $.state.map(_.reconnectId.foreach(dom.window.clearInterval))

      def closeWebSocket = $.state.map(
        _.ws.foreach(ws => {
          ws.onclose = _ => ()
          ws.close()
          println("[ws] closed")
        })
      ).attempt

      def clearWebSocket = $.modState(_.copy(ws = None))

      clearReconnecting >> closeWebSocket >> clearWebSocket
    }

  }

  val component = ScalaComponent.builder[Props]("Webcam")
    .initialState(State(None, None))
    .renderBackend[Backend]
    .componentDidMount(_.backend.start)
    .componentWillUnmount(_.backend.end)
    .build

  def apply(proxy: ModelProxy[Clicks]) = component(Props(proxy))
}
