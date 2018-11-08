package ohnosequences.db.rnacentral.test

import ohnosequences.db.rnacentral
import ohnosequences.fastarious.fasta._
import rnacentral.{EntryAnnotation, RNACentralData, RNAID, Version, iterators}
import java.io.File
import ohnosequences.s3.{Error => S3Error}
import rnacentral.s3Helpers.{getCheckedFileIfDifferent, getFile}

object data {

  def localFolder(version: rnacentral.Version): File =
    new File(s"./data/in/${version}/")

  private def getRnacentralData(
      version: rnacentral.Version): S3Error + RNACentralData = {
    val folder = localFolder(version)

    val fasta      = rnacentral.data.local.fastaFile(version, folder)
    val fastaS3    = rnacentral.data.idMappingTSV(version)
    val mappings   = rnacentral.data.local.idMappingFile(version, folder)
    val mappingsS3 = rnacentral.data.speciesSpecificFASTA(version)

    version match {
      case v: Version._9_0 =>
        getFile(mappingsS3, mappings)
          .flatMap(_ => getFile(fastaS3, fasta))
          .map(_ => RNACentralData(fasta, mappings))

      case v: Version._10_0 =>
        getCheckedFileIfDifferent(mappingsS3, mappings)
          .flatMap(_ => getCheckedFileIfDifferent(fastaS3, fasta))
          .map(_ => RNACentralData(fasta, mappings))
    }
  }

  lazy val rnaCentralData_9_0  = getRnacentralData(Version._9_0)
  lazy val rnaCentralData_10_0 = getRnacentralData(Version._10_0)

  def rnacentralData(version: rnacentral.Version): S3Error + RNACentralData =
    version match {
      case v: Version._9_0  => rnaCentralData_9_0
      case v: Version._10_0 => rnaCentralData_10_0
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
