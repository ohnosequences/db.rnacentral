package ohnosequences.apirnacentral.test

import org.scalatest.FunSuite

import ohnosequences.api.rnacentral._

class Parsing extends FunSuite {

  test("ID mapping tsv") {

    import java.io.File
    import com.github.tototoshi.csv._
    import IDMappingFile._

    val rows: File => Iterator[ParsingError + Row] =
      file =>
        (CSVReader.open(file)(tsv.format)).iterator map rowFrom
    
    val idMappingFile: File =
      new File("data/in/id_mapping.tsv")

    assert {
      true == rows(idMappingFile)
        .map(_.right.map(entryAnnotationFrom)) 
        .forall {
          case Left(err)  => false
          case Right(_)   => true
        }
    }
  }
}
