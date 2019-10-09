package ohnosequences.db.rnacentral

// the content of a row in `id_mappings.tsv`
final class Mapping(
    final val db: Database,
    final val taxID: TaxID,
    final val rnaType: RNAType,
    final val geneName: String
) {

  override def toString: String =
    s"${db}, ${taxID}, ${rnaType}, ${geneName}"
}

// the mappings for rnaID
final class RNAMappings(
    final val rnaID: RNAID,
    final val mappings: Array[Mapping]
) {

  override def toString: String =
    s"${rnaID}:\n${mappings.mkString("\n")}\n"
}

object RNAMappings {

  /// group by RNAID, then parse rest
  def from(data: RNACentralData): Iterator[RNAMappings] = {

    val f = iterators.segmentsFrom(extractRNAID _)
    f(IDMappings.rowsFrom(data.idMapping))
      .map({ case (id, rows) => new RNAMappings(id, rows.map(mapping _)) })
  }

  // tsv parsing
  ///////////////////////////////////////////////////////////////////////////////////////////
  def extractRNAID(row: Array[String]): RNAID       = RNAID string2RNAID row(0)
  def extractDatabase(row: Array[String]): Database = Database from row(1)
  def extractTaxID(row: Array[String]): TaxID       = row(3).toInt
  def extractRNAType(row: Array[String]): RNAType   = RNAType from row(4)
  def extractGeneName(row: Array[String]): String   = row(5)

  def mapping(row: Array[String]): Mapping =
    new Mapping(
      extractDatabase(row),
      extractTaxID(row),
      extractRNAType(row),
      extractGeneName(row)
    )
}
