
```scala
package era7bio.db

import better.files._

case object csvUtils {

  import com.github.tototoshi.csv._

  case object tableFormat extends TSVFormat {

    override val lineTerminator = "\n"
    // NOTE: this tsv has '\' inside fields
    override val escapeChar = '†'
  }

  def csvReader(file: File): CSVReader = CSVReader.open(file.toJava)(tableFormat)

  def lines(file: File): Iterator[Seq[String]] = csvReader(file) iterator

  // TODO much unsafe, add errors
  def rows(file: File)(headers: Seq[String]): Iterator[Map[String,String]] =
    lines(file) map { line => (headers zip line) toMap }


  // TODO: use a better representation for the table row
  type Row = Seq[String]

  implicit def rowOps(row: Row): RowOps = RowOps(row)
}

case class RowOps(row: csvUtils.Row) extends AnyVal {
  import csvUtils._, RNACentral5._

  def toMap: Map[Field, String] = Id2Taxa.keys.types.asList.zip(row).toMap

  def select(field: Field): String = this.toMap.apply(field)
}

```




[main/scala/blastDB.scala]: blastDB.scala.md
[main/scala/csvUtils.scala]: csvUtils.scala.md
[main/scala/rnaCentral.scala]: rnaCentral.scala.md
[test/scala/18sitsdatabase.scala]: ../../test/scala/18sitsdatabase.scala.md
[test/scala/compats.scala]: ../../test/scala/compats.scala.md
[test/scala/runBundles.scala]: ../../test/scala/runBundles.scala.md