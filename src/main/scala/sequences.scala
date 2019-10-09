package ohnosequences.db.rnacentral

import ohnosequences.dna._
import it.unimi.dsi.fastutil.longs.LongOpenHashSet

final case class RNASequence(
    val rnaID: RNAID,
    val sequence: DNA
)

object RNASequence {

  val cmp: Ordering[RNASequence] =
    Ordering.fromLessThan[RNASequence]((x, y) => x.rnaID <= y.rnaID)

  def sortedFrom(data: RNACentralData,
                 active: LongOpenHashSet): Array[RNASequence] = {
    val arr = from(data, active).toArray
    util.Sorting.quickSort(arr)(cmp)
    arr
  }

  def from(data: RNACentralData,
           active: LongOpenHashSet): Iterator[RNASequence] = {

    import ohnosequences.fastarious.fasta._

    // the active fasta not that active is
    ohnosequences.std.io.read
      .lines(data.activeFasta)
      .buffered
      .parseFasta
      .map { fa =>
        new RNASequence(
          RNAID.string2RNAID(fa.header.id),
          types.string2DNA(fa.sequence.letters)
        )
      }
      .filter(x => active contains x.rnaID)
  }
}
