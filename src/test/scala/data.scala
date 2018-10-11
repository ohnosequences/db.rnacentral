package ohnosequences.db.rnacentral.test

import ohnosequences.db.rnacentral
import ohnosequences.fastarious.fasta._
import rnacentral.{EntryAnnotation, RNACentralData, RNAID, Version, iterators}
import java.io.File
import ohnosequences.s3.{Error => S3Error}

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

  def rnacentralData(version: rnacentral.Version): S3Error + RNACentralData = {
    val fasta      = fastaLocalFile(version)
    val fastaS3    = rnacentral.data.idMappingTSV(version)
    val mappings   = idMappingLocalFile(version)
    val mappingsS3 = rnacentral.data.speciesSpecificFASTA(version)

    getCheckedFileIfDifferent(mappingsS3, mappings)
      .flatMap(_ => getCheckedFileIfDifferent(fastaS3, fasta))
      .map(_ => RNACentralData(fasta, mappings))
  }

  def fastas(version: Version): S3Error + Iterator[(RNAID, Seq[FASTA])] =
    rnacentralData(version).map(rnacentral.sequences.fastaByRNAID)

  def annotations(
      version: Version
  ): S3Error + Iterator[(RNAID, Set[EntryAnnotation])] =
    rnacentralData(version).map { rnacentralData =>
      rnacentral.IDMapping entryAnnotationsByRNAID {
        iterators.right {
          rnacentral.IDMapping entryAnnotations (
            iterators.right(rnacentral.IDMapping rows rnacentralData)
          )
        }
      }
    }
}
