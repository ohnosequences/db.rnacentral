
package ohnosequences.db.rnacentral.test

import java.io.file

class MirrorInS3 extends FunSuite {

  test("Check previous releases") {

    Version.all 
      .map { v => (v, data everything v) } 
      .foreach { case (v,objs) =>
        // TODO check files are there
        ???
      }
  }

  test("Mirror latest release") {

    val version = Version.latest

    val tmpFolder = 
      new File(s"./data/${version.name}/")

    val tmpIdMappingTSVGZFile =
      new File(tmp, data.input.idMappingTSVGZ)
    
    val tmpIdMappingTSVFile =
      new File(tmp, data.input.idMappingTSV)

    val tmpSpeciesSpecificFASTAFile =
      new File(tmp, data.input.speciesSpecificFASTA)

    assert {    
        OK == failFast(
        downloadTo(idMappingTSVGZURL, tmpIdMappingTSVGZFile) >=>
        extractTo(tmpIdMappingTSVGZFile, tmpIdMappingTSVFile) >=>
        uploadTo(tmpIdMappingTSVFile, data idMappingTSV version)
      )
    }

    // analogously for FASTA
  }
}