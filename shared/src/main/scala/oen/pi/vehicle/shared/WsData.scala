package oen.pi.vehicle.shared

import upickle.implicits.key
import upickle.default._

sealed trait WsData

@key("video-frame") case class VideoFrame(frameType: String, frame: String) extends WsData

object WsData {

  implicit val wsDataRW: ReadWriter[WsData] = ReadWriter.merge(macroRW[VideoFrame])

  def toJson(data: WsData): String = {
    write(data)
  }

  def fromJson(json: String): WsData = {
    read[WsData](json)
  }
}
