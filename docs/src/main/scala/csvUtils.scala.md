
```scala
package era7bio.db

import better.files._

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
  import csvUtils._, rnacentral.RNACentral5._

  def toMap: Map[Field, String] = Id2Taxa.keys.types.asList.zip(row).toMap

  def select(field: Field): String = this.toMap.apply(field)
}

```




[main/scala/blastDB.scala]: blastDB.scala.md
[main/scala/collectionUtils.scala]: collectionUtils.scala.md
[main/scala/csvUtils.scala]: csvUtils.scala.md
[main/scala/filterData.scala]: filterData.scala.md
[main/scala/rnacentral/compats.scala]: rnacentral/compats.scala.md
[main/scala/rnacentral/rnaCentral.scala]: rnacentral/rnaCentral.scala.md
[test/scala/runBundles.scala]: ../../test/scala/runBundles.scala.md