package oen.pi.vehicle.js.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.pi.vehicle.js.services.Clicks
import oen.pi.vehicle.shared.{VideoFrame, WsData}
import org.scalajs.dom
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}

import scala.scalajs.js

object Webcam {
  val protocol = if ("http:" == dom.window.location.protocol) "ws://" else "wss://"
  val url = protocol + dom.window.location.host +  "/webcam/ws"

  case class State(ws: Option[WebSocket], videoFrame: Option[VideoFrame])

  case class Props(proxy: ModelProxy[Clicks])

  class Backend($: BackendScope[Props, State]) {
    def render(state: State, props: Props) = {
      <.div(^.cls := "content-head is-center",
        state.videoFrame.fold(
          <.div("Loading webcam...")
        )( v =>
          <.div(<.img(^.src := s"${v.frameType}${v.frame}"))
        ),
        Home(props.proxy)
      )
    }

    def start: Callback = {

      def connect = CallbackTo[WebSocket] {
        val direct = $.withEffectsImpure

        def onopen(e: Event): Unit = {
          println("[ws] connected")
        }

        def onmessage(e: MessageEvent): Unit = {
          WsData.fromJson(e.data.toString) match {
            case v: VideoFrame => direct.modState(_.copy(videoFrame = Some(v)))
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
        }

        val ws = new WebSocket(url)
        ws.onopen = onopen _
        ws.onclose = onclose _
        ws.onmessage = onmessage _
        ws.onerror = onerror _
        ws
      }

      connect.attempt.flatMap {
        case Right(ws) => Callback(println(s"[ws] connecting to $url")) >> $.modState(_.copy(ws = Some(ws)))
        case Left(error) => Callback(println(s"[ws] error connecting: ${error.getMessage}"))
      }
    }

    def end: Callback = {
      def closeWebSocket = $.state.map(_.ws.foreach(_.close())).attempt

      def clearWebSocket = $.modState(_.copy(ws = None))

      closeWebSocket >> clearWebSocket
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
