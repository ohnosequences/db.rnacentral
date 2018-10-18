package ohnosequences.db.rnacentral

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import ohnosequences.s3.{S3Object, request}
import java.io.File

private[rnacentral] case object s3Helpers {

  val s3Client     = AmazonS3ClientBuilder.standard().build()
  val partSize5MiB = 5 * 1024 * 1024

  /**
    * A partial application of the s3 getCheckedFile function, using the S3
    * client declared above.
    */
  def getCheckedFile(s3Obj: S3Object, file: File) =
    request.getCheckedFile(s3Client)(s3Obj, file)

  /**
    * A partial application of the s3 paranoidPutFile function, using the S3
    * client and the part size declared above.
    */
  def paranoidPutFile(file: File, s3Obj: S3Object) =
    request.paranoidPutFile(s3Client)(file, s3Obj, partSize5MiB)(
      data.hashingFunction
    )

  def getCheckedFileIfDifferent(s3Obj: S3Object, file: File) =
    request.getCheckedFileIfDifferent(s3Client)(s3Obj, file)

  def objectExists(s3Obj: S3Object) =
    request.objectExists(s3Client)(s3Obj)
}
