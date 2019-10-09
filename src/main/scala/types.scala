package ohnosequences.db.rnacentral

import java.io.File
import ohnosequences.dna._

final case class RNACentralData(
    val idMapping: File,
    val speciesSpecificHeaders: File,
    val activeFasta: File,
)

final case class BinaryRNACentralData(
    val rnaHeaders: File,
    val rnaMappings: File,
    val rnaSequences: File
)

class RNACentralSubset(
    val rnaHeaders: RNAID2Headers,
    val rnaMappings: RNAID2Mappings,
    val rnaSequences: RNAID2Sequence
) {
  
  def only(xs: Array[RNAID]): RNACentralSubset = {

    val len = xs.length
    val h   = new RNAID2Headers(len)
    val m   = new RNAID2Mappings(len)
    val s   = new RNAID2Sequence(len)

    xs foreach { id =>
      h.put(id, rnaHeaders(id))
      m.put(id, rnaMappings(id))
      s.put(id, rnaSequences(id))
    }

    new RNACentralSubset(h, m, s)
  }

  def filterHeaders(p: Array[Header] => Boolean): RNACentralSubset =
    // val ids = ???
    // val len = ids.length

    // val h = new RNAID2Headers(len)
    // val m = new RNAID2Mappings(len)
    // val s = new RNAID2Sequence(len)

    // ids foreach { id =>
    //   h.put(id, rnaHeaders(id))
    //   m.put(id, rnaMappings(id))
    //   s.put(id, rnaSequences(id))
    // }

    // RNACentralSubset(h,m,s)
    ???
}

object TaxID {}

object RNAID {

  final val digits: Int    = 10
  final val prefix: String = "URS"

  final val zeroes: Array[String] = Array.tabulate(digits)(i => "0" * i)

  final def toFixedWidthHexString(r: RNAID): String = {

    val hexv = java.lang.Long.toString(r, 16).toUpperCase
    zeroes(digits - hexv.length).concat(hexv)
  }

  final def toString(r: RNAID): String =
    prefix concat toFixedWidthHexString(r)

  @inline final def hexString2RNAID(x: String): RNAID =
    java.lang.Long.parseLong(x, 16)

  final def string2RNAID(x: String): RNAID =
    hexString2RNAID(x stripPrefix prefix)

  final def RNAIDTaxID2String(x: RNAID, y: TaxID): String =
    toString(x)
      .concat("_")
      .concat(y.toString)
}

object types {

  final def cleanSequenceString(x: String): String =
    x.toUpperCase.replace('U', 'T')

  final val ATCGChars: Set[Char] = Set('A', 'T', 'C', 'G')

  final def isATCG(c: Char): Boolean =
    (c == 'A') ||
      (c == 'T') ||
      (c == 'C') ||
      (c == 'G')

  final def onlyATCG(x: String): Boolean =
    x forall (isATCG _)

  def string2DNA(s: String): DNA = {

    val x = cleanSequenceString(s)
    if (onlyATCG(x)) DNA.dnaFromCharsOrG(x.toCharArray) else DNA.empty
  }
}

sealed class RNAType(final val name: String)

case object RNAType {

  case object rRNA          extends RNAType("rRNA")
  case object tRNA          extends RNAType("tRNA")
  case object tmRNA         extends RNAType("tmRNA")
  case object precursorRNA  extends RNAType("precursor_RNA")
  case object telomeraseRNA extends RNAType("telomerase_RNA")
  case object ncRNA         extends RNAType("ncRNA")
  case object mRNA          extends RNAType("mRNA")
  case object antisenseRNA  extends RNAType("antisense_RNA")
  case object autocataliticallySplicedIntron
      extends RNAType("autocatalytically_spliced_intron")
  case object ribozyme           extends RNAType("ribozyme")
  case object hammerheadRibozyme extends RNAType("hammerhead_ribozyme")
  case object lncRNA             extends RNAType("lncRNA")
  case object lncRNABidirectionalPromoter
      extends RNAType("bidirectional_promoter_lncrna")
  case object RNasePRNA   extends RNAType("RNase_P_RNA")
  case object RNaseMRPRNA extends RNAType("RNase_MRP_RNA")
  case object guideRNA    extends RNAType("guide_RNA")
  case object rasiRNA     extends RNAType("rasiRNA")
  case object sRNA        extends RNAType("sRNA")
  case object scRNA       extends RNAType("scRNA")
  case object siRNA       extends RNAType("siRNA")
  case object miRNA       extends RNAType("miRNA")
  case object piRNA       extends RNAType("piRNA")
  case object snoRNA      extends RNAType("snoRNA")
  case object snRNA       extends RNAType("snRNA")
  case object SRPRNA      extends RNAType("SRP_RNA")
  case object vaultRNA    extends RNAType("vault_RNA")
  case object YRNA        extends RNAType("Y_RNA")
  case object miscRNA     extends RNAType("misc_RNA")
  case object other       extends RNAType("other")
  case object none        extends RNAType("none") // missing from annotation

  def from(x: String): RNAType =
    x match {
      case rRNA.name  => rRNA
      case tRNA.name  => tRNA
      case ncRNA.name => ncRNA
      case mRNA.name  => mRNA
      case autocataliticallySplicedIntron.name =>
        autocataliticallySplicedIntron
      case ribozyme.name                    => ribozyme
      case hammerheadRibozyme.name          => hammerheadRibozyme
      case lncRNA.name                      => lncRNA
      case lncRNABidirectionalPromoter.name => lncRNABidirectionalPromoter
      case RNasePRNA.name                   => RNasePRNA
      case RNaseMRPRNA.name                 => RNaseMRPRNA
      case guideRNA.name                    => guideRNA
      case rasiRNA.name                     => rasiRNA
      case scRNA.name                       => scRNA
      case sRNA.name                        => sRNA
      case siRNA.name                       => siRNA
      case miRNA.name                       => miRNA
      case piRNA.name                       => piRNA
      case snoRNA.name                      => snoRNA
      case snRNA.name                       => snRNA
      case SRPRNA.name                      => SRPRNA
      case YRNA.name                        => YRNA
      case miscRNA.name                     => miscRNA
      case tmRNA.name                       => tmRNA
      case precursorRNA.name                => precursorRNA
      case telomeraseRNA.name               => telomeraseRNA
      case vaultRNA.name | "vaultRNA"       => vaultRNA
      case antisenseRNA.name | "antisense"  => antisenseRNA
      case other.name                       => other
      case _                                => none
    }
}

sealed class Database(val name: String)

case object Database {

  case object dictyBase  extends Database("DICTYBASE")
  case object ENA        extends Database("ENA")
  case object Ensembl    extends Database("ENSEMBL")
  case object FlyBase    extends Database("FLYBASE")
  case object GENCODE    extends Database("GENCODE")
  case object GreenGenes extends Database("GREENGENES")
  case object GtRNAdb    extends Database("GTRNADB")
  case object HGNC       extends Database("HGNC")
  case object LNCipedia  extends Database("LNCIPEDIA")
  case object lncRNAdb   extends Database("LNCRNADB")
  case object MGI        extends Database("MGI")
  case object miRBase    extends Database("MIRBASE")
  case object Modomics   extends Database("MODOMICS")
  case object NONCODE    extends Database("NONCODE")
  case object PDBe       extends Database("PDB")
  case object PomBase    extends Database("POMBASE")
  case object RDP        extends Database("RDP")
  case object RefSeq     extends Database("REFSEQ")
  case object Rfam       extends Database("RFAM")
  case object RGD        extends Database("RGD")
  case object SGD        extends Database("SGD")
  case object SILVA      extends Database("SILVA")
  case object snOPY      extends Database("SNOPY")
  case object SRPDB      extends Database("SRPDB")
  case object TAIR       extends Database("TAIR")
  case object tmRNA_Web  extends Database("TMRNA_WEB")
  case object WormBase   extends Database("WORMBASE")
  case object Unknown    extends Database("unknown")

  def from(x: String): Database =
    x match {
      case dictyBase.name  => dictyBase
      case ENA.name        => ENA
      case Ensembl.name    => Ensembl
      case FlyBase.name    => FlyBase
      case GENCODE.name    => GENCODE
      case GreenGenes.name => GreenGenes
      case GtRNAdb.name    => GtRNAdb
      case HGNC.name       => HGNC
      case LNCipedia.name  => LNCipedia
      case lncRNAdb.name   => lncRNAdb
      case MGI.name        => MGI
      case miRBase.name    => miRBase
      case Modomics.name   => Modomics
      case NONCODE.name    => NONCODE
      case PDBe.name       => PDBe
      case PomBase.name    => PomBase
      case RGD.name        => RGD
      case RDP.name        => RDP
      case RefSeq.name     => RefSeq
      case Rfam.name       => Rfam
      case SGD.name        => SGD
      case SILVA.name      => SILVA
      case snOPY.name      => snOPY
      case SRPDB.name      => SRPDB
      case TAIR.name       => TAIR
      case tmRNA_Web.name  => tmRNA_Web
      case WormBase.name   => WormBase
      case _               => Unknown
    }
}
