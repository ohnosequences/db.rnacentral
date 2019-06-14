package ohnosequences.db.rnacentral

import it.unimi.dsi.fastutil.objects.ObjectArrayList

final class Header(
    final val taxID: TaxID,
    final val text: String
)

final class RNAHeaders(
    final val rnaID: Long,
    final val headers: Array[Header]
)

object RNAHeaders {

  final def extractRNAID(x: String): RNAID =
    RNAID hexString2RNAID x.slice(3, 13)

  def extractText(ln: String): String =
    new String(ln.dropWhile(_ != ' ').trim)

  def extractTaxID(ln: String): TaxID =
    ln.drop(14).takeWhile(_ != ' ').toInt

  def line2Header(ln: String): Header =
    new Header(extractTaxID(ln), extractText(ln))

  @rec
  def accInto(id: RNAID,
              it: BufferedIterator[String],
              xs: ObjectArrayList[Header]): Array[Header] =
    if (it.hasNext && extractRNAID(it.head) == id) {
      xs add line2Header(it.next)
      accInto(id, it, xs)
    } else xs.toArray(new Array[Header](xs.size))

  // NOTE assumes sorted input (!!)
  def parse(lines: BufferedIterator[String]): Array[RNAHeaders] = {

    val acc: ObjectArrayList[RNAHeaders]    = new ObjectArrayList
    val header_acc: ObjectArrayList[Header] = new ObjectArrayList

    @rec @inline
    def rec(i: Int): Array[RNAHeaders] =
      if (lines.hasNext) {
        val ln = lines.head
        val id = extractRNAID(ln)
        val hs = accInto(id, lines, header_acc)
        header_acc.clear
        acc add new RNAHeaders(id, hs)
        if (i % 10000 == 0) { println(s"processed ${i} ids") }
        rec(i + 1)
      } else acc.toArray(new Array[RNAHeaders](acc.size))

    rec(0)
  }
}
