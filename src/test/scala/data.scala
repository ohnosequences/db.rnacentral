package ohnosequences.db.rnacentral.test

import ohnosequences.db.rnacentral, rnacentral.Error
import ohnosequences.files.directory
import ohnosequences.fastarious.fasta._
import rnacentral.{EntryAnnotation, RNACentralData, RNAID, Version, iterators}
import java.io.File
import rnacentral.s3Helpers.{getCheckedFileIfDifferent}

object data {

  def localFolder(version: rnacentral.Version): File =
    new File(s"./data/in/${version}/")

  private def getRnacentralData(
      version: rnacentral.Version
  ): Error + RNACentralData = {
    val folder = localFolder(version)

    val fasta      = rnacentral.data.local.fastaFile(folder)
    val fastaS3    = rnacentral.data.speciesSpecificFASTA(version)
    val mappings   = rnacentral.data.local.idMappingFile(folder)
    val mappingsS3 = rnacentral.data.idMappingTSV(version)

    val maybeDir =
      directory.createDirectory(folder).left.map(Error.FileError)

    maybeDir
      .flatMap(_ => getCheckedFileIfDifferent(mappingsS3, mappings))
      .flatMap(_ => getCheckedFileIfDifferent(fastaS3, fasta))
      .map(_ => RNACentralData(fasta, mappings))
  }

  lazy val rnaCentralData_9_0  = getRnacentralData(Version._9_0)
  lazy val rnaCentralData_10_0 = getRnacentralData(Version._10_0)

  def rnacentralData(version: rnacentral.Version): Error + RNACentralData =
    version match {
      case v: Version._9_0  => rnaCentralData_9_0
      case v: Version._10_0 => rnaCentralData_10_0
    }

  def fastas(version: Version): Error + Iterator[(RNAID, Seq[FASTA])] =
    rnacentralData(version).map(rnacentral.sequences.fastaByRNAID)

  def annotations(
      version: Version
  ): Error + Iterator[(RNAID, Set[EntryAnnotation])] =
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
