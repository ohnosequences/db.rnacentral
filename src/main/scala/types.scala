package ohnosequences.db.rnacentral

import java.io.File
import ohnosequences.dna._
import ohnosequences.bits._

final case class RNACentralData(
    val idMapping: File,
    val speciesSpecificFasta: File
)

object types {

  import it.unimi.dsi.fastutil.longs._
  import it.unimi.dsi.fastutil.ints._
  import it.unimi.dsi.fastutil.objects.ObjectArrayList

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

  def RNAID2TaxIDs(
      headers: Iterator[String]): Long2ObjectOpenHashMap[Array[Int]] = {

    val map = new Long2ObjectOpenHashMap[IntArrayList](12000000, .75F)

    headers.zipWithIndex foreach {
      case (h, n) =>
        val key   = extractRNAID(h)
        val value = extractTaxID(h)

        if (map.containsKey(key)) {
          val v = map.get(key)
          v add value
        } else {
          val v = new IntArrayList()
          v add value
          map.put(key, v)
        }

        if (n % 100000 == 0) {
          println(s"${n} headers")
        }
    }

    val res = new Long2ObjectOpenHashMap[Array[Int]](map.size, .75F)

    map.long2ObjectEntrySet.fastForEach { e =>
      val arrl = e.getValue
      res.put(e.getLongKey, arrl.toArray(new Array[Int](arrl.size)))
    }

    res
  }

  // headers without the initial '>'
  def buildFastaAnnotations(headers: Iterator[String])
    : Long2ObjectOpenHashMap[ObjectArrayList[FastaAnnotation]] = {

    val map = new Long2ObjectOpenHashMap[ObjectArrayList[FastaAnnotation]]

    headers.zipWithIndex foreach {
      case (h, n) =>
        val key    = extractRNAID(h)
        val fannot = new FastaAnnotation(extractTaxID(h), extractHeader(h))
        if (map.containsKey(key)) {
          val v = map.get(key)
          v add fannot
        } else {
          val v = new ObjectArrayList[FastaAnnotation](1)
          v add fannot
          map.put(key, v)
        }

        if (n % 100000 == 0) {
          println(s"${n} headers")
        }
    }

    map
  }

  final class FastaAnnotations(
      val xs: Array[FastaAnnotation],
      val starts: Array[Int],
      val stops: Array[Int],
      val id2Pos: Long2IntOpenHashMap
  )

  type ID2DNA = it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap[DNA]
  type ID2FastaAnnotations =
    it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap[Array[FastaAnnotation]]

  def buildFastaAnnotations(
      ids: Array[RNAID],
      annots: Array[FastaAnnotation]): FastaAnnotations = {

    val acc_starts = new IntArrayList
    val acc_stops  = new IntArrayList
    val acc_ids    = new LongArrayList

    @rec @inline
    def rec(i: Index): Unit =
      if (i < ids.length) {

        val id   = ids(i)
        val _end = ids.indexWhere(_ != id, i)
        val end  = if (_end == -1) ids.length else _end

        acc_ids add id
        acc_starts add i
        acc_stops add end
        rec(end)
      }

    rec(0)

    println(acc_ids.size)
    val _ids    = acc_ids.toArray(new Array[Long](acc_ids.size))
    val _id2pos = new Long2IntOpenHashMap(_ids.length)

    println(_ids.length)
    println(new LongOpenHashSet(_ids).size)

    _ids.iterator.zipWithIndex foreach {
      case (id, i) =>
        _id2pos.put(id, i)
    }

    new FastaAnnotations(
      xs = annots,
      starts = acc_starts.toArray(new Array[Int](acc_starts.size)),
      stops = acc_stops.toArray(new Array[Int](acc_stops.size)),
      id2Pos = _id2pos
    )
  }

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

      def fastaAnnotations(file: File): Array[FastaAnnotation] = {
        val i   = readStream(file)
        val res = fastaAnnotations(i)
        i.close
        res
      }
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
