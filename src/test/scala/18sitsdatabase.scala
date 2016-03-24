package era7bio.db

import org.scalatest.FunSuite

import era7bio.db._
import better.files._

class RNACentralFileWrangling extends FunSuite {

  lazy val rnaCentralFastaFile  = file"/home/edu/tmp/rnacentral_active.fasta"
  lazy val id2taxafullFile      = file"/home/edu/tmp/id_mapping.tsv"
  lazy val id2taxaFile          = file"/home/edu/tmp/id_mapping.simple.tsv"
  lazy val id2taxafilteredFile  = file"/home/edu/tmp/id_mapping.filtered.simple.tsv"
  lazy val errLogFile           = file"/home/edu/tmp/filter-inactive.log"

  test("drop some fields from id2taxa") {

    id2taxaFile.clear

    fileWrangling.dropSomeFields(id2taxafullFile, id2taxaFile)
  }

  test("keep only active ids") {

    errLogFile.clear
    id2taxafilteredFile.clear

    fileWrangling.filterInactiveIds(
      rnaCentralFastaFile,
      id2taxaFile,
      id2taxafilteredFile,
      errLogFile
    )
  }
}
