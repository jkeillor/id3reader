import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.Date

import com.jkeillor.id3.Types._
import com.jkeillor.id3.codecs._
import scodec._
import scodec.bits.BitVector

import scala.util.control.NonFatal

/**
  * Created by jkeillor on 1/16/16.
  */
object ID3 {
  var count: Int = 0
  def main(args: Array[String]) {
    val search = args(0)
    val start = new Date()
    val path = FileSystems.getDefault().getPath(search)
    Files.walkFileTree(path, visitor)
    val end = new Date()
    println(s"processing ${count} files took ${end.getTime() - start.getTime()}ms")
  }

  val visitor = new SimpleFileVisitor[Path]() {
    override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
      if (attrs.isRegularFile && file.toFile.toString.contains(".mp3")) {
        val f = Files.readAllBytes(file)
        try {
          val tagOption = ID3.decodeFrameHeader(BitVector(f))
          tagOption.foreach { tag =>
            count += 1
            tag.frames.foreach{ frame =>
              if(frame.header.id.startsWith("T")) {
                frame.data.getText.foreach{ text =>
                  println(s"${frame.header.id} : ${text}")
                }
              }
            }
          }
        } catch {
          case NonFatal(e) =>
            println(file)
            println(e.getMessage)
        }
      }
      FileVisitResult.CONTINUE
    }
  }

  def decodeFrameHeader(bits: BitVector): Option[ID3Tag] = {
    val slice = id3.toBitVector
    if (bits.containsSlice(slice)) {
      val tag = Codec.decode[ID3Tag](bits.slice(bits.indexOfSlice(id3.toBitVector), bits.length)).require.value
      Some(tag)
    } else {
      None
    }
  }

}
