package ohnosequences.db.rnacentral.test

import ohnosequences.db.rnacentral
import ohnosequences.fastarious.fasta._
import rnacentral.{EntryAnnotation, RNACentralData, RNAID, Version, iterators}
import java.io.File
import ohnosequences.s3.{Error => S3Error}
import rnacentral.s3Helpers.getCheckedFileIfDifferent

object data {

  def localFolder(version: rnacentral.Version): File =
    new File(s"./data/in/${version}/")

  def rnacentralData(version: rnacentral.Version): S3Error + RNACentralData = {
    val folder = localFolder(version)

    val fasta      = rnacentral.data.local.fastaFile(version, folder)
    val fastaS3    = rnacentral.data.idMappingTSV(version)
    val mappings   = rnacentral.data.local.idMappingFile(version, folder)
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
