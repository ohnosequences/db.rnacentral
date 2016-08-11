
## RNACentral data

We mirror RNACentral data at S3. There are important differences across versions: for example, the fields in the taxid mappings are different.


```scala
package ohnosequences.db.rnacentral

import ohnosequences.cosas._, types._, records._, klists._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._
import ohnosequences.statika._, aws._
import ohnosequences.fastarious._, fasta._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._

import com.github.tototoshi.csv._
import ohnosequences.db.csvUtils._
import ohnosequences.db.rnacentral._

abstract class AnyRNACentral(val version: String) {

  val metadata = ohnosequences.generated.metadata.db_rnacentral

  lazy val prefix =
    S3Folder("resources.ohnosequences.com", metadata.organization)/metadata.artifact/metadata.version/

  val fastaFileName:       String = s"rnacentral.${version}.fasta"
  val tableFileName:       String = s"table.${version}.tsv"

  lazy val fasta:       S3Object = prefix/fastaFileName
  lazy val table:       S3Object = prefix/tableFileName
}

case object RNACentral5 extends AnyRNACentral("5.0") {

  sealed trait Field extends AnyType {
    type Raw = String
    lazy val label = toString
  }

  case object id          extends Field
  case object db          extends Field
  case object external_id extends Field
  case object tax_id      extends Field
  // TODO use http://www.insdc.org/rna_vocab.html
  case object rna_type    extends Field
  case object gene_name   extends Field


  case object Id2Taxa extends RecordType(
    id          :×:
    db          :×:
    external_id :×:
    tax_id      :×:
    rna_type    :×:
    gene_name   :×:
    |[Field]
  )
}

```




[main/scala/blastDB.scala]: blastDB.scala.md
[main/scala/collectionUtils.scala]: collectionUtils.scala.md
[main/scala/csvUtils.scala]: csvUtils.scala.md
[main/scala/filterData.scala]: filterData.scala.md
[main/scala/rnacentral.scala]: rnacentral.scala.md
[test/scala/compats.scala]: ../../test/scala/compats.scala.md
[test/scala/generateData.scala]: ../../test/scala/generateData.scala.md
[test/scala/rnaCentral.scala]: ../../test/scala/rnaCentral.scala.md
[test/scala/runBundles.scala]: ../../test/scala/runBundles.scala.md