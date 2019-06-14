package ohnosequences.db.rnacentral

import ohnosequences.std.io
import io._

object headerLines {

  def apply(f: File): Iterator[String] =
    io.read lines f
}

object mappingsRows {

  import com.github.tototoshi.csv._

  def apply(f: File): Iterator[Array[String]] =
    CSVReader.open(f)(format).iterator.map(_.toArray)

  object format extends TSVFormat {

    override val lineTerminator: String =
      "\n"

    // NOTE: this tsv has '\' inside fields; we need to set it to something not used there
    override val escapeChar: Char =
      '†'
  }
}
