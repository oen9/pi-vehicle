package oen.pi.vehicle.jvm.webcam

import cats.effect.Sync
import com.github.sarxos.webcam.Webcam

object WebCamUtils {
  def acquireWebCam[F[_] : Sync](isDummy: Boolean): F[Option[Webcam]] = {
    if (isDummy) Sync[F].pure(None) else  Sync[F].delay(Some(Webcam.getDefault()))
  }
}
