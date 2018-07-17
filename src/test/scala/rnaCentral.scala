package ohnosequences.db.rnacentral.test

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, s3._
import ohnosequences.db.rnacentral._
import java.io.File

case object mirrorRNAcentral extends Bundle() {

  lazy val dataFolder =
    new File("/media/ephemeral0")

  lazy val speciesSpecificFASTAFile =
    new File(dataFolder, data.input.speciesSpecificFASTA)

  lazy val idMappingTSVFile =
    new File(dataFolder, data.input.idMappingTSV)

  private def download(url: String) =
    cmd("wget")(url)

  private def extract(file: String) =
    cmd("gzip")("-d", file)

  private def uploadToS3 =
    LazyTry {

      val s3client =
        s3.defaultClient

      s3client.upload(speciesSpecificFASTAFile, data.speciesSpecificFASTA)
      s3client.upload(idMappingTSVFile, data.idMappingTSV)
    }

  def instructions: AnyInstructions =
    download(data.input.speciesSpecificFASTAGZURL) -&-
      download(data.input.idMappingTSVGZURL) -&-
      extract(data.input.speciesSpecificFASTAGZ) -&-
      extract(data.input.idMappingTSVGZ) -&-
      uploadToS3 -&-
      say(s"RNACentral ${data.version} mirrored at ${data.prefix}")
}
