package ohnosequences.db.rnacentral

case object io {

  import scala.collection.JavaConverters._
  import java.nio.file.Files
  import com.github.tototoshi.csv._
  import java.io.File

  val lines: File => Iterator[String] =
    file => Files.lines(file.toPath).iterator.asScala

  val tsv: File => Iterator[Seq[String]] =
    file => CSVReader.open(file)(format).iterator

  case object format extends TSVFormat {

    override val lineTerminator: String =
      "\n"

    // NOTE: this tsv has '\' inside fields; we need to set it to something not used there
    override val escapeChar: Char =
      '†'
  }
}
