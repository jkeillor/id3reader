package com.jkeillor.id3

import com.jkeillor.id3.Types._
import scodec.Codec
import scodec.codecs._

/**
  * Created by jkeillor on 1/16/16.
  */
package object codecs {
  implicit val id3Header: Codec[ID3Header] = fixedSizeBytes(10, {
    ("label" | constant(id3)) :~>:
      ("version" | int(8)) ::
      ("revision" | int(8)) ::
      ("unsync" | bool) ::
      ("extended" | bool) ::
      ("experimental" | bool) ::
      ("footer" | bool) ::
      ("ignore" | ignore(4) ::
        ("size" | bytes(4)))
  }).as[ID3Header]

  implicit val id3v3FrameHeader: Codec[ID3FrameHeader] = {
    ("id" | fixedSizeBits(32, ascii)) ::
      ("size" | int32) ::
      ("tagPreserve" | ignore(16))
  }.as[ID3FrameHeader]
  implicit val id3v2FrameHeader: Codec[ID3FrameHeader] = {
    ("id" | fixedSizeBits(24, ascii)) ::
      ("size" | int(24))
  }.as[ID3FrameHeader]

  def id3FrameData(size: Int): Codec[ID3FrameData] = {
    ("encoding" | byte) ::
      ("text" | bytes(size))
  }.as[ID3FrameData]

  implicit val id3Frame: Codec[ID3Frame] = {
    ("header" | id3v3FrameHeader) >>:~ { hdr =>
      ("data" | id3FrameData(hdr.size - 1)).hlist
    }
  }.as[ID3Frame]

  implicit val idv32Frame: Codec[ID3Frame] = {
    ("header" | id3v2FrameHeader) >>:~ { hdr =>
      ("data" | id3FrameData(hdr.size - 1)).hlist
    }
  }.as[ID3Frame]

  val id3v3frameCodec: ID3FrameCodec = new ID3FrameCodec(id3Frame)
  val id3v2frameCodec: ID3FrameCodec = new ID3FrameCodec(idv32Frame)

  implicit val id3Tag: Codec[ID3Tag] = {
    ("header" | id3Header) >>:~ { hdr =>
      if (hdr.version == 2) {
        ("frames" | id3v2frameCodec).hlist
      } else {
        ("frames" | id3v3frameCodec).hlist
      }
    }
  }.as[ID3Tag]
}
