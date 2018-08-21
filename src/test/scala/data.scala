package ohnosequences.db.rnacentral.test

import ohnosequences.db.rnacentral
import ohnosequences.fastarious.fasta._
import rnacentral.{EntryAnnotation, RNACentralData, RNAID, Version, iterators}
import java.io.File

object data {

  def cleanLocalFolder(version: rnacentral.Version): Unit =
    localFolder(version).listFiles
      .filter(f => !(f.isDirectory))
      .foreach { _.delete }

  def localFolder(version: rnacentral.Version): File =
    new File(s"./data/in/${version}/")

  def idMappingLocalFile(version: rnacentral.Version): File =
    new File(localFolder(version), rnacentral.data.input.idMappingTSV)

  def idMappingGZLocalFile(version: rnacentral.Version): File =
    new File(localFolder(version), rnacentral.data.input.idMappingTSVGZ)

  def fastaLocalFile(version: rnacentral.Version): File =
    new File(localFolder(version), rnacentral.data.input.speciesSpecificFASTA)

  def fastaGZLocalFile(version: rnacentral.Version): File =
    new File(localFolder(version), rnacentral.data.input.speciesSpecificFASTAGZ)

  def rnacentralData(version: rnacentral.Version): RNACentralData =
    // TODO download and check if files are missing; check otherwise
    RNACentralData(
      speciesSpecificFasta = fastaLocalFile(version),
      idMapping = idMappingLocalFile(version),
    )

  def fastas(version: Version): Iterator[(RNAID, Seq[FASTA])] =
    rnacentral.sequences fastaByRNAID rnacentralData(version)

  def annotations(version: Version): Iterator[(RNAID, Set[EntryAnnotation])] =
    rnacentral.IDMapping entryAnnotationsByRNAID {
      iterators right {
        rnacentral.IDMapping entryAnnotations (
          iterators right (rnacentral.IDMapping rows rnacentralData(version))
        )
      }
    }
}
