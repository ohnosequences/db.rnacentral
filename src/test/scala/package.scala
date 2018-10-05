package ohnosequences.db.rnacentral

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import ohnosequences.s3.request
import com.amazonaws.services.s3.model.S3ObjectId
import java.io.File
import ohnosequences.db.rnacentral

package object test {

  def allRight[X, Y]: Iterator[X + Y] => Boolean =
    _.forall {
      case Left(_)  => false
      case Right(_) => true
    }

  private[test] val s3Client = AmazonS3ClientBuilder.standard().build()

  def getCheckedFile(s3Obj: S3ObjectId, file: File) =
    request.getCheckedFile(s3Client)(s3Obj, file)

  def paranoidPutFile(file: File, s3Obj: S3ObjectId) =
    request.paranoidPutFile(s3Client)(file, s3Obj, 5 * 1024 * 1024)(
      rnacentral.data.hashingFunction
    )
}
