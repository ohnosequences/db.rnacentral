package ohnosequences.db.rnacentral

import ohnosequences.files.{directory, gzip, remote}
import ohnosequences.s3.S3Object
import ohnosequences.std.io
import io._
import Data._

object mirror {

  def download[V <: Version](local: Local): Data[V] => Error + File =
    x =>
      local.inputToLocal(x) match {
        case (url, file) =>
          remote.download(url, file).left.map(Error.FileError(_))
    }

  def uncompress(fileFile: (File, File)): Error + File =
    gzip.uncompress(fileFile._1, fileFile._2).left.map(Error.FileError(_))

  def downloadFromRNACentralFTP[V <: Version](
      version: V,
      folder: File): Error + Seq[(Data[V], File)] = {

    val local = Local(folder)
    val dwnld = download[V](local)
    val dwnldAndData: Data[V] => Error + (Data[V], File) =
      d =>
        dwnld(d).right.map { f =>
          (d, f)
      }

    Error.failFast(input.all(version))(dwnldAndData)
  }

  def uploadToS3[V <: Version](version: V,
                               local: Local): Seq[Error] + Seq[Data[V]] =
    ???

  def downloadFromS3[V <: Version](version: V,
                                   folder: File): Seq[Error] + Seq[Data[V]] =
    ???

  def file(url: URL,
           gzFile: File,
           file: File,
           s3Obj: S3Object): Error + S3Object =
    remote
      .download(url, gzFile)
      .flatMap(gzFile => gzip.uncompress(gzFile, file))
      .fold(
        err => Left(Error.FileError(err)),
        file => s3Helpers.paranoidPutFile(file, s3Obj).map(_ => s3Obj)
      )
}

object headerLines {

  def apply(f: File): Iterator[String] =
    io.read lines f
}

object IDMappings {

  import com.github.tototoshi.csv._

  def rowsFrom(f: File): BufferedIterator[Array[String]] =
    CSVReader.open(f)(format).iterator.map(_.toArray).buffered

  object format extends TSVFormat {

    override val lineTerminator: String =
      "\n"

    // NOTE: this tsv has '\' inside fields; we need to set it to something not used there
    override val escapeChar: Char =
      '†'
  }
}
