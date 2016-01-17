package com.jkeillor.id3.codecs

import com.jkeillor.id3.Types.ID3Frame
import scodec._
import scodec.bits.BitVector
import scodec.codecs._

/**
  * Created by jkeillor on 1/16/16.
  */
class ID3FrameCodec(codec: Codec[ID3Frame]) extends Codec[List[ID3Frame]] {
  def sizeBound = SizeBound.unknown

  def encode(list: List[ID3Frame]) = Encoder.encodeSeq(codec)(list)

  def decode(buffer: BitVector) = {
    var bldr = List.newBuilder[ID3Frame]
    var remaining = buffer
    var error: Option[Err] = None
    //Look forward to see if there's another frame we can grab.
    while (remaining.nonEmpty &&
      peek[Unit](constant(0x00)).decode(remaining).isFailure &&
      peek[ID3Frame](codec).decode(remaining).isSuccessful &&
      error.isEmpty
    ) {
      codec.decode(remaining) match {
        case Attempt.Successful(DecodeResult(value, rest)) =>
          bldr += value
          remaining = rest
        case Attempt.Failure(err) =>
          error = Some(err)
      }
    }
    Attempt.fromErrOption(error, DecodeResult(bldr.result, BitVector.empty))
  }

  override def toString = s"IDV3FrameCodec($codec)"
}