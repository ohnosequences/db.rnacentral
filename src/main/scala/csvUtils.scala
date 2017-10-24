package ohnosequences.db

case object csvUtils {

  import com.github.tototoshi.csv._

  case object RNAcentralTSVFormat extends TSVFormat {

    override val lineTerminator = "\n"
    // NOTE: this tsv has '\' inside fields
    override val escapeChar = '†'
  }

  case object UnixCSVFormat extends DefaultCSVFormat {
    override val lineTerminator: String = "\n"
  }

  type Row = Seq[String]

  implicit def rowOps(row: Row): RowOps = RowOps(row)
}

case class RowOps(row: csvUtils.Row) extends AnyVal {
  import rnacentral.RNAcentral._

  def toMap: Map[Field, String] = Id2Taxa.keys.types.asList.zip(row).toMap

  def select(field: Field): String = this.toMap.apply(field)
}
