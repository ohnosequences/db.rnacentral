package ohnosequences.db.rnacentral.test

import ohnosequences.db.rnacentral.+
import sys.process._ // System process execution: !
import ohnosequences.awstools.s3, s3.S3Object
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import java.io.File

sealed trait Error {
  val msg: String
}

case object Error {
  final case class Download(val msg: String)     extends Error
  final case class Upload(val msg: String)       extends Error
  final case class Extract(val msg: String)      extends Error
  final case class DirCreation(val msg: String)  extends Error
  final case class FileNotFound(val msg: String) extends Error
}

case object utils {

  /**
    * Returns `Right(file)` if the download from `uri` to `file`
    * succeeded, `Left(Error.Download(msg))` otherwise.
    */
  def downloadFrom(uri: java.net.URI, file: File): Error.Download + File = {
    val command = s"wget ${uri.toString} -O ${file.getCanonicalPath}"
    val result  = command !

    if (result == 0)
      Right(file)
    else
      Left(Error.Download(s"Error downloading $uri to $file."))
  }

  /**
    * Returns `Right(file)` if the download from `s3Object` to `file`
    * succeeded, `Left(Error.Download(msg))` otherwise.
    */
  def downloadFromS3(s3Object: S3Object, file: File): Error.Download + File = {
    println(s"Downloading $s3Object to $file.")
    val tm = TransferManagerBuilder
      .standard()
      .withS3Client(s3.defaultClient)
      .build()

    scala.util.Try {
      tm.download(
          s3Object.bucket,
          s3Object.key,
          file
        )
        .waitForCompletion()
    } match {
      case scala.util.Success(s) => Right(file)
      case scala.util.Failure(e) => Left(Error.Download(e.toString))
    }
  }

  /**
    * Returns `Right(outputDir)` if the extraction from `input` into `outputDir`
    * succeeded, ``Left(Error.Extract(msg))`` otherwise.
    */
  def uncompressAndExtractTo(input: File,
                             outputDir: File): Error.Extract + File =
    if (!outputDir.isDirectory)
      Left(
        Error.Extract(
          s"Error extracting $input into directory $outputDir: " +
            s"$outputDir is not a directory."
        )
      )
    else {
      val command =
        s"gzip -d ${input.getCanonicalPath}"

      val result = command !

      if (result == 0)
        Right(outputDir)
      else
        Left(
          Error.Extract(
            s"Error extracting $input into directory $outputDir: " +
              s"the command finished with errors."
          )
        )
    }

  /**
    * Returns `Right(s3Object)` if the upload from `file` to `s3Object`
    * succeeded, ``Left(Error.Upload(msg))`` otherwise.
    */
  def uploadTo(file: File, s3Object: S3Object): Error.Upload + S3Object = {

    val tm = TransferManagerBuilder
      .standard()
      .withS3Client(s3.defaultClient)
      .build()

    scala.util.Try {
      tm.upload(
          s3Object.bucket,
          s3Object.key,
          file
        )
        .waitForCompletion()
    } match {
      case scala.util.Success(s) => Right(s3Object)
      case scala.util.Failure(e) => Left(Error.Upload(e.toString))
    }
  }

  /**
    * Returns `Right(directory)` if it was possible to create all directories
    * in `directory` (or if they already existed);
    * `Left(Error.DirCreation(msg))` otherwise.
    */
  def createDirectory(directory: File): Error.DirCreation + File =
    if (!directory.exists)
      if (directory.mkdirs())
        Right(directory)
      else
        Left(Error.DirCreation(s"Error creating directory $directory."))
    else
      Right(directory)

  /**
    * Returns `Right(Iterator[String])` if it was possible to read the lines from the file, `Left(Error.FileNotFound(msg))` otherwise.
    */
  def retrieveLinesFrom(file: File): Error.FileNotFound + Iterator[String] =
    if (file.exists)
      Right(io.Source.fromFile(file.getCanonicalPath).getLines)
    else
      Left(Error.FileNotFound(s"Error reading $file: file does not exist."))
}
