package ohnosequences.db.rnacentral

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import ohnosequences.s3.{S3Object, request}
import ohnosequences.files.digest.DigestFunction
import java.io.File

/**
  * Partial applications of functions from `s3`, using a standard S3Client
  * built here, [[s3Helpers.s3Client]], and with a default part size,
  * [[s3Helpers.partSize5MiB]].
  */
object s3Helpers {

  val hashingFunction: DigestFunction = DigestFunction.SHA512
  lazy val s3Client                   = AmazonS3ClientBuilder.standard().build()
  val partSize5MiB                    = 5 * 1024 * 1024

  def getFile(s3Obj: S3Object, file: File) =
    request.getFile(s3Client)(s3Obj, file).left.map(Error.S3Error)

  def getCheckedFile(s3Obj: S3Object, file: File) =
    request.getCheckedFile(s3Client)(s3Obj, file).left.map(Error.S3Error)

  def paranoidPutFile(file: File, s3Obj: S3Object) =
    request
      .paranoidPutFile(s3Client)(file, s3Obj, partSize5MiB)(hashingFunction)
      .left
      .map(Error.S3Error)

  def getCheckedFileIfDifferent(s3Obj: S3Object, file: File) =
    request
      .getCheckedFileIfDifferent(s3Client)(s3Obj, file)
      .left
      .map(Error.S3Error)

  def objectExists(s3Obj: S3Object) =
    request.objectExists(s3Client)(s3Obj).left.map(Error.S3Error)
}
