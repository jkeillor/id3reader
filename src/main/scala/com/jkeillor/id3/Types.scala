package com.jkeillor.id3

import java.nio.charset.StandardCharsets
import scodec.bits.ByteVector

/**
  * Created by jkeillor on 1/16/16.
  */
object Types {
  val id3 = ByteVector("ID3".map(_.toByte))

  case class ID3Header(version: Int,
                       revision: Int,
                       unsync: Boolean,
                       extended: Boolean,
                       experimental: Boolean,
                       footer: Boolean,
                       size: ByteVector)

  //TODO: distinguish the various frame types and handle appropriately
  case class ID3Frame(header: ID3FrameHeader, data: ID3FrameData)
  case class ID3FrameHeader(id: String, size: Int)
  case class ID3FrameData(encoding: Byte, text: ByteVector) {
    //This seems to work for most of the "standard" text information frames
    def getText: Option[String] = {
      implicit var charset = if (encoding == 0x01) {
        StandardCharsets.UTF_16
      } else {
        StandardCharsets.UTF_8
      }
      text.decodeString(charset).right.toOption
    }
  }

  case class ID3Tag(header: ID3Header, frames: List[ID3Frame])
}
