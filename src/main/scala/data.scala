package ohnosequences.db.rnacentral

import ohnosequences.s3.{S3Object, s3AddressFromString}
import java.io.File

sealed abstract class Version(val name: String) {
  override final def toString: String = name
}

object Version {

  lazy val all: Set[Version] =
    Set(_12)
  // Set(_9_0, _10_0)

  type _12 = _12.type
  case object _12 extends Version("12")

  type _9_0 = _9_0.type
  case object _9_0 extends Version("9.0")
  type _10_0 = _10_0.type
  case object _10_0 extends Version("10.0")
}

sealed abstract class Data[V <: Version] {

  def version: V
  def name: String
  override final def toString = version.toString / name
}

object Data {

  case class IDMapping[V <: Version](val version: V) extends Data[V] {
    val name = "id_mapping".tsv
  }
  case class SpeciesSpecificSequences[V <: Version](val version: V)
      extends Data[V] {
    val name = "rnacentral_species_specific_ids".fasta
  }
  case class ActiveSequences[V <: Version](val version: V) extends Data[V] {
    val name = "rnacentral_active"
  }

  case class GZip[V <: Version](val d: Data[V]) extends Data[V] {
    val version = d.version
    val name    = d.name.gz
  }

  def everythingGZ[V <: Version](version: V): Seq[Data[V]] =
    everything(version) map { v =>
      GZip(v)
    }

  def everything[V <: Version](version: V): Seq[Data[V]] =
    Seq(
      IDMapping(version),
      SpeciesSpecificSequences(version),
      ActiveSequences(version)
    )

  case object input {

    val base: URL =
      new URL("ftp", "ftp.ebi.ac.uk/", "pub/databases/RNAcentral")

    def release[V <: Version](v: V): URL =
      base / "releases" / v.toString

    def url[V <: Version](x: Data[V]): URL =
      x match {
        case GZip(x) =>
          x match {
            case SpeciesSpecificSequences(_) | ActiveSequences(_) =>
              release(x.version) / "sequences" / x.name
            case IDMapping(_) =>
              release(x.version) / "id_mapping" / x.name
          }
        case _ => throw new IllegalArgumentException
      }

    def urlToS3[V <: Version](x: Data[V]): (URL, S3Object) =
      url(x) -> s3.s3Object(x)

    def all[V <: Version](v: V): Seq[Data[V]] =
      everythingGZ(v)
  }

  case object s3 {

    def prefix[V <: Version](v: V): S3Object =
      s3"resources.ohnosequences.com" /
        "ohnosequences" /
        "db" /
        "rnacentral" /
        "unstable" /
        v.toString

    def s3Object[V <: Version](x: Data[V]): S3Object =
      prefix(x.version) / x.name
  }

  case class Local(val folder: File) {

    def prefix[V <: Version](v: V): File =
      new File(folder, s"${v}/")

    def file[V <: Version](x: Data[V]): File =
      new File(prefix(x.version), x.name)

    def localToS3[V <: Version](x: Data[V]): (File, S3Object) =
      file(x) -> s3.s3Object(x)

    def s3ToLocal[V <: Version](x: Data[V]): (S3Object, File) =
      localToS3(x).swap

    def inputToLocal[V <: Version](x: Data[V]): (URL, File) =
      input.url(x) -> file(x)
  }
}
