package ohnosequences.db.rnacentral.test

import ohnosequences.db.rnacentral
import ohnosequences.db.rnacentral._
import java.io.File
import java.net.URI
import ohnosequences.test.ReleaseOnlyTest
import org.scalatest.FunSuite
import ohnosequences.awstools.s3, s3.ScalaS3Client

class MirrorInS3 extends FunSuite {

  private val s3Client = ScalaS3Client(s3.defaultClient)

  test("Check previous releases") {

    val vData = Version.all map { v =>
      (v, rnacentral.data everything v)
    }

    vData foreach {
      case (v, objs) =>
        println(s"checking ${v} data:")
        println(s"  ${objs}")
        assert { objs forall s3Client.objectExists _ }
    }
  }

  test("Mirror latest release", ReleaseOnlyTest) {

    val version = Version.latest

    import utils._
    import rnacentral.data.input

    data cleanLocalFolder version

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
}
