package oen.pi.vehicle.jvm.webcam

import java.io.ByteArrayOutputStream
import java.util.Base64

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Effect, Fiber, IO}
import cats.implicits._
import com.github.sarxos.webcam.Webcam
import fs2.Stream
import fs2.concurrent.Topic
import javax.imageio.ImageIO
import oen.pi.vehicle.jvm.webcam.WebcamService.CancellableJob
import oen.pi.vehicle.shared.{VideoFrame, WsData}
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text

import scala.concurrent.ExecutionContextExecutorService
import scala.concurrent.duration.DurationDouble

class WebcamService[F[_] : Effect](webcam: Option[Webcam],
                                   blockingEc: ExecutionContextExecutorService,
                                   dummyCamRef: Ref[F, CancellableJob[IO]],
                                   realCamRef: Ref[F, CancellableJob[IO]],
                                   topic: Topic[F, VideoFrame]) {

  private[this] val blockingCS = IO.contextShift(blockingEc)
  private[this] val blockingTimer = IO.timer(blockingEc)

  def turnOnCam(): F[Unit] = for {
    _ <- turnOnRealCam()
  } yield ()

  def turnOffCam(): F[Unit] = for {
    _ <- turnOnDummyCam()
  } yield ()

  def createFrameStream(): Stream[F, WebSocketFrame] = topic.subscribe(10).map(frame => Text(WsData.toJson(frame)))

  private[this] def turnOnRealCam(): F[Unit] = for {
    _ <- turnOffRealCam()
    _ <- turnOffDummyCam()
    realCancellable <- webcam.fold(Effect[F].pure(none: CancellableJob[IO]))(webcam => {
      webcam.open()
      runRealCam(webcam).start(blockingCS).map(_.some).to[F]
    })
    _ <- realCamRef.set(realCancellable)
  } yield ()

  private[this] def turnOnDummyCam(): F[Unit] = for {
    _ <- turnOffRealCam()
    _ <- turnOffDummyCam()
    dummyCancellable <- runDummyCam().start(blockingCS).to[F]
    _ <- dummyCamRef.set(Some(dummyCancellable))
  } yield ()

  private[this] def turnOffRealCam(): F[Unit] = for {
    oldRealToCancel <- realCamRef.get
    _ <- oldRealToCancel.fold(Effect[F].unit)(_.cancel.to[F])
    _ <- webcam.fold(Effect[F].unit)(w => Effect[F].delay(w.close()))
    _ <- realCamRef.set(None)
  } yield ()

  private[this] def turnOffDummyCam(): F[Unit] = for {
    oldDummyToCancel <- dummyCamRef.get
    _ <- oldDummyToCancel.fold(Effect[F].unit)(_.cancel.to[F])
    _ <- dummyCamRef.set(None)
  } yield ()

  private[this] def runRealCam(webcam: Webcam): IO[Unit] = {
    (for {
      _ <- blockingTimer.sleep(16.milliseconds)
      videoFrame <- takeAndPrepareRealFrame(webcam)
      _ <- Effect[F].toIO(topic.publish1(videoFrame))
    } yield ()).flatMap(_ => runRealCam(webcam))
  }

  private[this] def takeAndPrepareRealFrame(webcam: Webcam): IO[VideoFrame] = IO.delay {
    val img = webcam.getImage
    val baos = new ByteArrayOutputStream()
    ImageIO.write(img, "JPG", baos)
    val base64 = new String(Base64.getEncoder.encode(baos.toByteArray), "UTF8")
    VideoFrame("data:image/jpeg;base64,", base64)
  }

  private[this] def runDummyCam(): IO[Unit] = {
    implicit val timer = blockingTimer
    (for {
      e <- FakeCamUtils.createFakeCamStream[IO](30.milliseconds)
      _ <- Stream.eval(Effect[F].toIO(topic.publish1(e)))
    } yield ()).compile.drain
  }
}

object WebcamService {
  type CancellableJob[F[_]] = Option[Fiber[F, Unit]]

  def apply[F[_] : ConcurrentEffect](webcam: Option[Webcam], blockingEc: ExecutionContextExecutorService): F[WebcamService[F]] = for {
    dummyCamRef <- Ref.of[F, CancellableJob[IO]](None)
    realCamRef <- Ref.of[F, CancellableJob[IO]](None)
    topic <- Topic[F, VideoFrame](VideoFrame("", ""))
    webcamService = new WebcamService[F](webcam, blockingEc, dummyCamRef, realCamRef, topic)
    _ <- webcamService.turnOffCam()
  } yield webcamService
}
