package ohnosequences.api.rnacentral

case object tsv {

  import com.github.tototoshi.csv._

  case object format extends TSVFormat {

    override val lineTerminator: String = 
      "\n"

    // NOTE: this tsv has '\' inside fields; we need to set it to something not used there
    override val escapeChar: Char = 
      '†'
  }
}
