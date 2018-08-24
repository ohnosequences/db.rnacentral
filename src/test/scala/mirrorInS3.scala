package ohnosequences.db.rnacentral.test

import ohnosequences.db.rnacentral
import ohnosequences.db.rnacentral._
import java.net.URI
import ohnosequences.test.ReleaseOnlyTest
import org.scalatest.FunSuite
import ohnosequences.awstools.s3, s3.ScalaS3Client

class MirrorInS3 extends FunSuite {

  private val s3Client = ScalaS3Client(s3.defaultClient)

  private def mirrorVersion(version: Version): Unit = {
    import rnacentral.data.input
    import utils._

    // ID Mapping
    assertResult(Right(rnacentral.data.idMappingTSV(version))) {
      downloadFrom(
        new URI(input.idMappingTSVGZURL(version)),
        data.idMappingGZLocalFile(version)
      ).right
        .flatMap { file =>
          uncompressAndExtractTo(file, data.localFolder(version))
        }
        .right
        .flatMap { file =>
          uploadTo(data.idMappingLocalFile(version),
                   rnacentral.data.idMappingTSV(version))
        }
    }

    // FASTA
    assertResult(Right(rnacentral.data.speciesSpecificFASTA(version))) {
      downloadFrom(
        new URI(input.speciesSpecificFASTAGZURL(version)),
        data.fastaGZLocalFile(version)
      ).right
        .flatMap { file =>
          uncompressAndExtractTo(file, data.localFolder(version))
        }
        .right
        .flatMap { file =>
          uploadTo(data.fastaLocalFile(version),
                   rnacentral.data.speciesSpecificFASTA(version))
        }
    }
  }

  test("Check and mirror all releases", ReleaseOnlyTest) {
    Version.all foreach { version =>
      val objs = rnacentral.data everything version

      println(s"Checking ${version} data:")
      println(s"  ${objs}")

      if (!(objs forall s3Client.objectExists _)) {
        println(s"  Mirroring $version data...")

        data cleanLocalFolder version
        mirrorVersion(version)

        println(s"  $version data mirrored.")
      }
    }
  }
}
