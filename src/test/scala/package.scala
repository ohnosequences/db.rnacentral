package ohnosequences.db.rnacentral

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import ohnosequences.s3.request
import com.amazonaws.services.s3.model.S3ObjectId
import java.io.File
import ohnosequences.db.rnacentral

package object test {

  type +[A, B] = Either[A, B]

  def allRight[X, Y]: Iterator[X + Y] => Boolean =
    _.forall {
      case Left(_)  => false
      case Right(_) => true
    }

  private[test] val s3Client = AmazonS3ClientBuilder.standard().build()
  private val partSize5MiB   = 5 * 1024 * 1024

  /**
    * A partial application of the s3 getCheckedFile function, using the S3
    * client declared above.
    */
  private[test] def getCheckedFile(s3Obj: S3ObjectId, file: File) =
    request.getCheckedFile(s3Client)(s3Obj, file)

  /**
    * A partial application of the s3 paranoidPutFile function, using the S3
    * client and the part size declared above.
    */
  private[test] def paranoidPutFile(file: File, s3Obj: S3ObjectId) =
    request.paranoidPutFile(s3Client)(file, s3Obj, partSize5MiB)(
      rnacentral.data.hashingFunction
    )

  private[test] def getCheckedFileIfDifferent(s3Obj: S3ObjectId, file: File) =
    request.getCheckedFileIfDifferent(s3Client)(s3Obj, file)
}
