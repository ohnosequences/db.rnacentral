
```scala
package era7bio.db

import better.files._

case object csvUtils {

  import com.github.tototoshi.csv._

  case object funnyTSV extends CSVFormat {

  val delimiter: Char = '\t'
  val quoteChar: Char = '"'
  // this tsv has '\' inside fields
  val escapeChar: Char = '†'

  val lineTerminator: String = "\n"
  val quoting: Quoting = QUOTE_NONE
  val treatEmptyLineAsNil: Boolean = false
}

  def csvReader(file: File): CSVReader = CSVReader.open(file.toJava)(funnyTSV)

  def lines(file: File): Iterator[Seq[String]] = csvReader(file) iterator

  // TODO much unsafe, add errors
  def rows(file: File)(headers: Seq[String]): Iterator[Map[String,String]] =
    lines(file) map { line => (headers zip line) toMap }
}

```




[test/scala/18sitsdatabase.scala]: ../../test/scala/18sitsdatabase.scala.md
[main/scala/runBundles.scala]: runBundles.scala.md
[main/scala/rnacentralrelease.scala]: rnacentralrelease.scala.md
[main/scala/csvUtils.scala]: csvUtils.scala.md