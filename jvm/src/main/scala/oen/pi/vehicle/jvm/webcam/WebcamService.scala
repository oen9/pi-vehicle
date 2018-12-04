package oen.pi.vehicle.jvm.webcam

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Effect, Fiber, IO}
import oen.pi.vehicle.jvm.webcam.WebcamService.CancellableJob
import cats.implicits._
import com.github.sarxos.webcam.Webcam
import fs2.concurrent.Topic
import fs2.Stream
import oen.pi.vehicle.shared.{VideoFrame, WsData}
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text

import scala.concurrent.ExecutionContextExecutorService
import scala.concurrent.duration.DurationDouble

class WebcamService[F[_] : Effect](webcam: Webcam,
                                   blockingEc: ExecutionContextExecutorService,
                                   dummyCamRef: Ref[F, CancellableJob[IO]],
                                   realCamRef: Ref[F, CancellableJob[IO]],
                                   topic: Topic[F, VideoFrame]) {

  private[this] val blockingCS = IO.contextShift(blockingEc)
  private[this] val blockingTimer = IO.timer(blockingEc)

  def turnOnDummyCam(): F[Unit] = for {
    _ <- turnOffRealCam()
    _ <- turnOffDummyCam()
    _ <- Effect[F].delay(println("starting dummy webcam..."))
    dummyCancellable <- runDummyCam().start(blockingCS).to[F] // TODO only when dummyCamRef None OR always run turnOffDummyCam() as is now
    _ <- dummyCamRef.set(Some(dummyCancellable))
  } yield ()

  def turnOffRealCam(): F[Unit] = for {
    oldRealToCancel <- realCamRef.get
    _ <- oldRealToCancel.fold(Effect[F].unit)(_.cancel.to[F])
    _ <- realCamRef.set(None)
  } yield ()

  def turnOffDummyCam(): F[Unit] = for {
    oldDummyToCancel <- dummyCamRef.get
    _ <- oldDummyToCancel.fold(Effect[F].unit)(_.cancel.to[F])
    _ <- dummyCamRef.set(None)
  } yield ()

  def createFrameStream(): Stream[F, WebSocketFrame] = topic.subscribe(10).map(frame => Text(WsData.toJson(frame)))

  private[this] def runDummyCam(): IO[Unit] = {
    implicit val timer = blockingTimer
    (for {
      e <- FakeCamUtils.createFakeCamStream[IO](30.milliseconds)
      _ <- Stream.eval(Effect[F].toIO(topic.publish1(e)))
    } yield()).compile.drain
  }
}

object WebcamService {
  type CancellableJob[F[_]] = Option[Fiber[F, Unit]]

  def apply[F[_] : ConcurrentEffect](webcam: Webcam, blockingEc: ExecutionContextExecutorService): F[WebcamService[F]] = for {
    dummyCamRef <- Ref.of[F, CancellableJob[IO]](None)
    realCamRef <- Ref.of[F, CancellableJob[IO]](None)
    topic <- Topic[F, VideoFrame](VideoFrame("", ""))
    webcamService = new WebcamService[F](webcam, blockingEc, dummyCamRef, realCamRef, topic)
    _ <- webcamService.turnOnDummyCam()
  } yield webcamService
}
