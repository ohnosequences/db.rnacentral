package ohnosequences.db.rnacentral

import java.io.File
import ohnosequences.dna._
import ohnosequences.bits._

final case class RNACentralData(
    val idMapping: File,
    val speciesSpecificFasta: File
)

object types {

  // fasta -> Map[RNAID, Array[FastaAnnotation]]
  //   tsv -> Map[RNAID, Array[TSVAnnotation]]
  //
  // the sequences are in a Map[RNAID, DNA]
  //
  // It is fairly easy to generate subsets: simply remove all entries in
  // the maps above by `RNAID`.
  type RNAID  = Long
  type TaxID  = Int
  type Header = String
  type Index  = Int
  type rec    = annotation.tailrec

  final class FastaAnnotation(
      final val taxID: TaxID,
      final val header: Header
  )

  final class TSVAnnotation(
      final val db: Database,
      final val db_ref: String,
      final val taxID: TaxID,
      final val rnaType: RNAType,
      final val geneName: String
  )

  final val RNAIDDigits: Int    = 10
  final val RNAIDPrefix: String = "URS"

  final val zeroes: Array[String] =
    Array.tabulate(RNAIDDigits) { i =>
      "0" * i
    }

  final def RNAID2FixedWidthHexString(r: RNAID): String = {

    val hexv = java.lang.Long.toString(r, 16).toUpperCase
    zeroes(RNAIDDigits - hexv.length).concat(hexv)
  }

  def RNAID2String(r: RNAID): String =
    RNAIDPrefix.concat(RNAID2FixedWidthHexString(r))

  @inline final def hexString2RNAID(x: String): RNAID =
    java.lang.Long.parseLong(x, 16)

  final def string2RNAID(x: String): RNAID =
    hexString2RNAID(x.stripPrefix(RNAIDPrefix))

  final def RNAIDTaxID2String(x: RNAID, y: TaxID): String =
    RNAID2String(x)
      .concat("_")
      .concat(y.toString)

  final def extractRNAID(x: String): RNAID =
    hexString2RNAID(x.slice(3, 13))

  final def extractTaxID(x: String): TaxID =
    x.drop(14).takeWhile(_ != ' ').toInt

  final def extractHeader(x: String): Header =
    x.dropWhile(_ != ' ').trim

  final def description2Header(x: String): Header =
    x

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

  object serialization {

    import java.io.{
      DataInput,
      DataInputStream,
      DataOutput,
      DataOutputStream,
      File,
      FileInputStream,
      FileOutputStream
    }
    import it.unimi.dsi.fastutil.io.{
      BinIO,
      FastBufferedInputStream,
      FastBufferedOutputStream
    }

    object write {

      def writeStream(f: File): DataOutputStream =
        new DataOutputStream(
          new FastBufferedOutputStream(new FileOutputStream(f)))

      def array[X](
          xs: Array[X],
          o: DataOutputStream,
          w: (X, DataOutputStream) => DataOutputStream
      ): DataOutputStream = {

        @rec @inline
        def rec(i: Index, o: DataOutputStream): DataOutputStream =
          if (i < xs.length)
            rec(i + 1, w(xs(i), o))
          else
            o

        if (xs.isEmpty) o
        else {
          o writeInt xs.length
          rec(0, o)
        }
      }

      def longs(xs: Array[Long], o: DataOutputStream): DataOutputStream = {

        @rec @inline
        def rec(i: Int): DataOutputStream =
          if (i < xs.length) {
            o writeLong xs(i)
            rec(i + 1)
          } else o

        rec(0)
      }

      def dna(x: DNA, o: DataOutputStream): DataOutputStream = {

        o writeLong x.length
        longs(x.leftBits.words, o)
        longs(x.rightBits.words, o)
      }

      def dnas(xs: Array[DNA], o: DataOutputStream): DataOutputStream =
        array[DNA](xs, o, dna _)

      def RNAID(x: RNAID, o: DataOutputStream): DataOutputStream = {
        o.writeLong(x)
        o
      }

      def RNAIDs(xs: Array[RNAID], o: DataOutputStream): DataOutputStream =
        array[RNAID](xs, o, RNAID _)

      def taxID(x: TaxID, o: DataOutputStream): DataOutputStream = {
        o.writeInt(x)
        o
      }

      def taxIDs(xs: Array[TaxID], o: DataOutputStream): DataOutputStream =
        array[TaxID](xs, o, taxID _)

      def header(x: Header, o: DataOutputStream): DataOutputStream =
        array[Char](x.toCharArray, o, (c, o) => { o.writeChar(c); o })

      def fastaAnnotation(f: FastaAnnotation,
                          o: DataOutputStream): DataOutputStream = {
        taxID(f.taxID, o)
        header(f.header, o)
      }

      def fastaAnnotations(fs: Array[FastaAnnotation],
                           o: DataOutputStream): DataOutputStream =
        array[FastaAnnotation](fs, o, fastaAnnotation _)
    }

    object read {

      def readStream(f: File): DataInputStream =
        new DataInputStream(new FastBufferedInputStream(new FileInputStream(f)))

      def array[X: scala.reflect.ClassTag](
          r: DataInputStream,
          b: DataInputStream => X
      ): Array[X] = {

        val l   = r.readInt
        val arr = new Array[X](l)

        @inline @rec
        def rec(i: Int): Array[X] =
          if (i < arr.length) {
            arr(i) = b(r)
            rec(i + 1)
          } else
            arr

        rec(0)
      }

      def longs(r: DataInputStream, xs: Array[Long]): Array[Long] = {

        @rec @inline
        def rec(i: Int): Array[Long] =
          if (i < xs.length) {
            xs(i) = r.readLong
            rec(i + 1)
          } else xs

        rec(0)
      }

      def dna(r: DataInputStream): DNA = {

        val len      = r.readLong
        val numWords = BitVector.numWords(len)

        val leftWs  = new Array[Long](numWords)
        val rightWs = leftWs.clone

        val left  = new BitVector(longs(r, leftWs), len)
        val right = new BitVector(longs(r, rightWs), len)

        new DNA(leftBits = left, rightBits = right)
      }

      def dnas(r: DataInputStream): Array[DNA] =
        array[DNA](r, dna _)

      def RNAID(i: DataInputStream): RNAID =
        i.readLong

      def taxID(i: DataInputStream): TaxID =
        i.readInt

      def RNAIDs(i: DataInputStream): Array[RNAID] =
        array[RNAID](i, RNAID _)

      def taxIDs(i: DataInputStream): Array[TaxID] =
        array[TaxID](i, taxID _)

      def header(i: DataInputStream): Header =
        new String(array[Char](i, _.readChar))

      def fastaAnnotation(i: DataInputStream): FastaAnnotation = {
        val x = taxID(i)
        val y = header(i)
        new FastaAnnotation(x, y)
      }

      def fastaAnnotations(i: DataInputStream): Array[FastaAnnotation] =
        array[FastaAnnotation](i, fastaAnnotation _)
    }
  }
}

final case class RNASequence(
    val rnaID: RNAID,
    val sequence: String
)

final case class Entry(
    val rnaSequence: RNASequence,
    val sequenceAnnotations: Set[SequenceAnnotation],
    val entryAnnotations: Set[EntryAnnotation]
)

final case class EntryAnnotation(
    val rnaID: RNAID,
    val ncbiTaxonomyID: TaxonID,
    val databaseEntry: DatabaseEntry,
    val rnaType: RNAType,
    val geneName: Option[String]
)

final case class DatabaseEntry(
    val database: Database,
    val id: String
)

final case class SequenceAnnotation(
    val rnaID: RNAID,
    val ncbiTaxonomyID: TaxonID,
    val description: String
)

sealed class RNAType(val name: String)

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

  val from: String => Option[RNAType] =
    _ match {
      case rRNA.name  => Some(rRNA)
      case tRNA.name  => Some(tRNA)
      case ncRNA.name => Some(ncRNA)
      case mRNA.name  => Some(mRNA)
      case autocataliticallySplicedIntron.name =>
        Some(autocataliticallySplicedIntron)
      case ribozyme.name                    => Some(ribozyme)
      case hammerheadRibozyme.name          => Some(hammerheadRibozyme)
      case lncRNA.name                      => Some(lncRNA)
      case lncRNABidirectionalPromoter.name => Some(lncRNABidirectionalPromoter)
      case RNasePRNA.name                   => Some(RNasePRNA)
      case RNaseMRPRNA.name                 => Some(RNaseMRPRNA)
      case guideRNA.name                    => Some(guideRNA)
      case rasiRNA.name                     => Some(rasiRNA)
      case scRNA.name                       => Some(scRNA)
      case sRNA.name                        => Some(sRNA)
      case siRNA.name                       => Some(siRNA)
      case miRNA.name                       => Some(miRNA)
      case piRNA.name                       => Some(piRNA)
      case snoRNA.name                      => Some(snoRNA)
      case snRNA.name                       => Some(snRNA)
      case SRPRNA.name                      => Some(SRPRNA)
      case YRNA.name                        => Some(YRNA)
      case miscRNA.name                     => Some(miscRNA)
      case tmRNA.name                       => Some(tmRNA)
      case precursorRNA.name                => Some(precursorRNA)
      case telomeraseRNA.name               => Some(telomeraseRNA)
      case vaultRNA.name | "vaultRNA"       => Some(vaultRNA)
      case antisenseRNA.name | "antisense"  => Some(antisenseRNA)
      case other.name                       => Some(other)
      case _                                => None
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

  val from: String => Option[Database] =
    _ match {
      case dictyBase.name  => Some(dictyBase)
      case ENA.name        => Some(ENA)
      case Ensembl.name    => Some(Ensembl)
      case FlyBase.name    => Some(FlyBase)
      case GENCODE.name    => Some(GENCODE)
      case GreenGenes.name => Some(GreenGenes)
      case GtRNAdb.name    => Some(GtRNAdb)
      case HGNC.name       => Some(HGNC)
      case LNCipedia.name  => Some(LNCipedia)
      case lncRNAdb.name   => Some(lncRNAdb)
      case MGI.name        => Some(MGI)
      case miRBase.name    => Some(miRBase)
      case Modomics.name   => Some(Modomics)
      case NONCODE.name    => Some(NONCODE)
      case PDBe.name       => Some(PDBe)
      case PomBase.name    => Some(PomBase)
      case RGD.name        => Some(RGD)
      case RDP.name        => Some(RDP)
      case RefSeq.name     => Some(RefSeq)
      case Rfam.name       => Some(Rfam)
      case SGD.name        => Some(SGD)
      case SILVA.name      => Some(SILVA)
      case snOPY.name      => Some(snOPY)
      case SRPDB.name      => Some(SRPDB)
      case TAIR.name       => Some(TAIR)
      case tmRNA_Web.name  => Some(tmRNA_Web)
      case WormBase.name   => Some(WormBase)
      case _               => None
    }
}
