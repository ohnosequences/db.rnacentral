package ohnosequences.db.rnacentral

import it.unimi.dsi.fastutil.objects.ObjectArrayList

// 8B + 4B + 4B + 4B + 4B = 24B + header
final class IDMappingRow(
    final val db: Database,
    final val taxID: TaxID,
    final val rnaType: RNAType,
    final val geneName: String
)

final class RNAMappings(
    final val rnaID: RNAID,
    final val mappings: Array[IDMappingRow]
)

object RNAMappings {

  def extractRNAID(row: Array[String]): RNAID =
    types.string2RNAID(row(0))

  def extractDatabase(row: Array[String]): Database =
    Database.from(row(1))

  def extractTaxID(row: Array[String]): TaxID =
    row(3).toInt

  def extractRNAType(row: Array[String]): RNAType =
    RNAType.from(row(4))

  def extractGeneName(row: Array[String]): String =
    row(5)

  def idMappingRow(row: Array[String]): IDMappingRow =
    new IDMappingRow(
      extractDatabase(row),
      extractTaxID(row),
      extractRNAType(row),
      extractGeneName(row)
    )

  def accInto(id: RNAID,
              it: BufferedIterator[Array[String]],
              xs: ObjectArrayList[IDMappingRow]): Array[IDMappingRow] =
    if (it.hasNext && extractRNAID(it.head) == id) {
      xs add idMappingRow(it.next)
      accInto(id, it, xs)
    } else xs.toArray(new Array[IDMappingRow](xs.size))

  def parse(rows: BufferedIterator[Array[String]]): Array[RNAMappings] = {

    val acc: ObjectArrayList[RNAMappings]           = new ObjectArrayList
    val mappings_acc: ObjectArrayList[IDMappingRow] = new ObjectArrayList

    @rec @inline
    def rec(i: Int): Array[RNAMappings] =
      if (rows.hasNext) {
        val row = rows.head
        val id  = extractRNAID(row)
        val hs  = accInto(id, rows, mappings_acc)
        mappings_acc.clear
        acc add new RNAMappings(id, hs)
        if (i % 10000 == 0) { println(s"processed ${i} ids") }
        rec(i + 1)
      } else acc.toArray(new Array[RNAMappings](acc.size))

    rec(0)
  }
}
