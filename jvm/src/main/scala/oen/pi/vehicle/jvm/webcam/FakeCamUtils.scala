package oen.pi.vehicle.jvm.webcam

import java.io.ByteArrayOutputStream
import java.util.Base64

import cats.Functor
import cats.effect.Timer
import fs2.Stream
import javax.imageio.ImageIO
import oen.pi.vehicle.shared.{VideoFrame, WsData}
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text

import scala.concurrent.duration.FiniteDuration

object FakeCamUtils {
  def createFakeCamStream[F[_] : Functor : Timer](d: FiniteDuration): Stream[F, WebSocketFrame] = {
    val ticks: Stream[F, Stream[F, WebSocketFrame]] = Stream.awakeEvery[F](d).map(_ => Stream.empty)
    val frames: Stream[F, Stream[F, WebSocketFrame]] = Stream(readSprite(): _*).map(s => {
      val videoFrameJson = WsData.toJson(VideoFrame("data:image/png;base64,", s))
      Stream(Text(videoFrameJson))
    }).repeat

    ticks.interleave(frames).flatten
  }

  def readSprite(): Seq[String] = {
    val fullImg = ImageIO.read(getClass.getResourceAsStream("/spritesheet.png"))
    val widthParts = 4
    val heightParts = 4
    val width = fullImg.getWidth / widthParts
    val height = fullImg.getHeight / heightParts

    for {
      hIdx <- 0 until heightParts
      wIdx <- 0 until widthParts
    } yield {
      val img = fullImg.getSubimage(wIdx * width, hIdx * height, width, height)
      val baos = new ByteArrayOutputStream()
      ImageIO.write(img, "PNG", baos)
      new String(Base64.getEncoder.encode(baos.toByteArray), "UTF8")
    }
  }
}
