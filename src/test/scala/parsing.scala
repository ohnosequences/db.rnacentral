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

    def rightEntryAnnotations: Iterator[(RNAID, EntryAnnotation)] = 
      rows(idMappingFile)
      .collect { case Right(row) => entryAnnotationFrom(row) }
      .collect { case Right(x) => x }
    
    def groupedAnnotations =
      iterators.segmentsFrom({ e: (RNAID, EntryAnnotation) => e._1 })(rightEntryAnnotations)

    groupedAnnotations foreach {
      case (id, annots) =>
        println { s"${id} has ${annots.length} annotations" }
    }

    // assert {
    //   true == rows(idMappingFile)
    //     .map(_.right.map(entryAnnotationFrom)) 
    //     .forall {
    //       case Left(err)  => false
    //       case Right(_)   => true
    //     }
    // }
  }
}
