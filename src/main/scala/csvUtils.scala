package ohnosequences.db

import rnacentral.RNAcentralField

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
}

case class Row(
  fields: List[RNAcentralField],
  values: Seq[String]
) {

  def toMap: Map[RNAcentralField, String] = fields.zip(values).toMap

  def select(field: RNAcentralField): String = this.toMap.apply(field)
}
