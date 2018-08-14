package ohnosequences.db.rnacentral.test

import ohnosequences.db.rnacentral
import ohnosequences.db.rnacentral._
import java.io.File
import ohnosequences.test.ReleaseOnlyTest
import org.scalatest.FunSuite

class MirrorInS3 extends FunSuite {

  test("Check previous releases") {

    Version.all
      .map { v =>
        (v, rnacentral.data everything v)
      }
      .foreach {
        case (v, objs) =>
          // TODO check files are there
          ???
      }
  }

  test("Mirror latest release", ReleaseOnlyTest) {

    val version = Version.latest

    val tmpFolder =
      new File(s"./data/${version.name}/")

    val tmpIdMappingTSVGZFile =
      new File(tmpFolder, rnacentral.data.input.idMappingTSVGZ)

    val tmpIdMappingTSVFile =
      new File(tmpFolder, rnacentral.data.input.idMappingTSV)

    val tmpSpeciesSpecificFASTAFile =
      new File(tmpFolder, rnacentral.data.input.speciesSpecificFASTA)

    // TODO needs code for files and S3 interaction
    // assert {
    //   Right(OK) == failFast(
    //     downloadTo(idMappingTSVGZURL, tmpIdMappingTSVGZFile) >=>
    //       extractTo(tmpIdMappingTSVGZFile, tmpIdMappingTSVFile) >=>
    //       uploadTo(tmpIdMappingTSVFile, data idMappingTSV version)
    //   )
    // }

    // analogously for FASTA
  }
}
