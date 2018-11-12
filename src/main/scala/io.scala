package ohnosequences.db.rnacentral

case object io {

  import scala.collection.JavaConverters._
  import java.nio.file.Files
  import com.github.tototoshi.csv._
  import java.io.File

  /**
    * Build an iterator that yields the lines of the input file one by one.
    */
  val lines: File => Iterator[String] =
    file => Files.lines(file.toPath).iterator.asScala

  /**
    * Build an iterator that yields the lines of the input TSV file one by one,
    * with the fields separated in different elements of a sequence.
    */
  val tsv: File => Iterator[Seq[String]] =
    file => CSVReader.open(file)(format).iterator

  /**
    * TSV format used by
    * <a href="https://github.com/tototoshi/scala-csv">scala-csv</a>
    */
  case object format extends TSVFormat {

    override val lineTerminator: String =
      "\n"

    // NOTE: this tsv has '\' inside fields; we need to set it to something not used there
    override val escapeChar: Char =
      '†'
  }
}
