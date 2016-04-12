
```scala
package era7bio.db

import ohnosequences.cosas._, types._, records._, klists._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._
import ohnosequences.statika._, aws._
import better.files._
import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._
import era7.defaults._, loquats._
```


## RNACentral data

We mirror RNACentral data at S3. There are important differences across versions: for example, the fields in the taxid mappings are different.


```scala
abstract class AnyRNAcentral(val version: String) {

  lazy val prefix = S3Object("resources.ohnosequences.com","")/"rnacentral"/version/

  val fastaFileName           : String = s"rnacentral.${version}.fasta"
  val id2taxaFileName         : String = s"id2taxa.${version}.tsv"
  val id2taxaactiveFileName   : String = s"id2taxa.active.${version}.tsv"

  lazy val fasta          : S3Object = prefix/fastaFileName
  lazy val id2taxa        : S3Object = prefix/id2taxaFileName
  lazy val id2taxaactive  : S3Object = prefix/id2taxaactiveFileName
}

case object RNACentral5 extends AnyRNAcentral("5.0") {

  case object id            extends Type[String]("id")
  case object db            extends Type[String]("db")
  case object external_id   extends Type[String]("external_id")
  case object tax_id        extends Type[String]("tax_id")
  // TODO use http://www.insdc.org/rna_vocab.html
  case object rna_type      extends Type[String]("rna_type")
  case object gene_name     extends Type[String]("gene_name")


  case object Id2Taxa extends RecordType(
    id          :×:
    db          :×:
    external_id :×:
    tax_id      :×:
    rna_type    :×:
    gene_name   :×:
    |[AnyType]
  )
}
```


### Mirror RNACentral release files in S3

This bundle

1. downloads all RNACentral raw files from the EBI ftp
2. creates other id2taxa file containing only the *active* sequences (those actually found in RNACentral)
3. uploads everything to S3


```scala
case object MirrorRNAcentralRelease extends Bundle() {

  val rnaCentral: RNACentral5.type = RNACentral5
  lazy val dataFolder = file"/media/ephemeral0"

  lazy val rnaCentralFastaFile    = dataFolder/"rnacentral_active.fasta"
  lazy val rnaCentralFastaFileGz  = dataFolder/"rnacentral_active.fasta.gz"
  lazy val id2taxaFileGz          = dataFolder/"id_mapping.tsv.gz"
  lazy val id2taxaFile            = dataFolder/"id_mapping.tsv"
  lazy val id2taxaactiveFile      = dataFolder/rnaCentral.id2taxaactiveFileName
  lazy val errLogFile             = dataFolder/s"${toString}.log"

  lazy val getRnaCentralFastaFileGz = cmd("wget")(
    s"ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral/releases/${rnaCentral.version}/sequences/rnacentral_active.fasta.gz"
  )
  lazy val extractRnaCentralFastaFile = cmd("gzip")("-d", rnaCentralFastaFileGz.name)

  lazy val getRnaCentralIdMappingGz = cmd("wget")(
    s"ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral/releases/${rnaCentral.version}/id_mapping/id_mapping.tsv.gz"
  )
  lazy val extractRnaCentralIdMapping = cmd("gzip")("-d", id2taxaFileGz.name)

  def instructions: AnyInstructions =
    // get raw input stuff from EBI FTP
    getRnaCentralFastaFileGz -&- extractRnaCentralFastaFile -&-
    getRnaCentralIdMappingGz -&- extractRnaCentralIdMapping -&-
    LazyTry[String] {
    // drop inactive ids from the id2taxa file
    fileWrangling.filterInactiveIds(
      rnaCentralFastaFile = rnaCentralFastaFile,
      id2taxaFile         = id2taxaFile,
      id2taxaactiveFile   = id2taxaactiveFile,
      errLogFile          = errLogFile
    )

    val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

    // upload the uncompressed fasta file
    transferManager.upload(
      rnaCentral.fasta.bucket, rnaCentral.fasta.key,
      rnaCentralFastaFile.toJava
    )
    .waitForCompletion

    // upload full id2taxa file
    transferManager.upload(
      rnaCentral.id2taxa.bucket, rnaCentral.id2taxa.key,
      id2taxaFile.toJava
    )
    .waitForCompletion

    // upload active id2taxa file
    transferManager.upload(
      rnaCentral.id2taxaactive.bucket, rnaCentral.id2taxaactive.key,
      id2taxaactiveFile.toJava
    )
    .waitForCompletion

    s"RNACentral version ${rnaCentral.version} mirrored at ${rnaCentral.prefix} including active-only id2taxa mapping"
  }
}

case object fileWrangling {

  import RNACentral5._
```


### Filter inactive ids from the id2taxa file

This method keeps from the id2taxa csv those present in the active RNACentral fasta. It will fail if at least **one** fasta id is not found in the id2taxa csv.

> **WARNING** This implementation reuses iterators, something about which the Scala std docs say: "Using the old iterator is undefined, subject to change, and may result in changes to the new iterator as well."
>
> I tried to do it in a different way but I couldn't. `iterator.span` is crap, as other iterator methods:
>
> - http://grokbase.com/t/gg/scala-user/12bknnwmbg/why-does-this-iterator-cause-a-stack-overflow#20121119kgsf2s3oryv5xbp2z62sa7cg4e
> - https://issues.scala-lang.org/browse/SI-9332
> - https://issues.scala-lang.org/browse/SI-5838
> - https://groups.google.com/forum/#!topic/scala-user/s3UutoCEckg


```scala
  def filterInactiveIds(
    rnaCentralFastaFile : File,
    id2taxaFile         : File,
    id2taxaactiveFile   : File,
    errLogFile          : File
  )
  : Unit = {

    import ohnosequences.fastarious._, fasta._

    // here we drop any errors from the taxid mapping file
    val allIds = csvUtils.rows(id2taxaFile)(Id2Taxa.keys.types map typeLabel asList)
      .map(Id2Taxa.parse(_))
      .collect({ case Right(rec) => rec })

    fasta.parseFastaDropErrors(rnaCentralFastaFile.lines).foreach(

      fa => {

        val faId = fa.getV(header).id
        // advance ids, get current
        val rest   = allIds.dropWhile( _.getV(id) != faId )

        if(allIds.hasNext) {
          // we are just getting the first assignment here
          val rec = rest.next
          id2taxaactiveFile << s"${rec.getV(id)}\t${rec.getV(db)}\t${rec.getV(external_id)}\t${rec.getV(tax_id)}\t${rec.getV(rna_type)}\t${rec.getV(gene_name)}"
        }
        else {

          errLogFile << s"${fa.getV(header)} not found in ${id2taxaFile}. All subsequent will fail"
        }
      }
    )
  }
}

```




[test/scala/18sitsdatabase.scala]: ../../test/scala/18sitsdatabase.scala.md
[main/scala/runBundles.scala]: runBundles.scala.md
[main/scala/rnacentralrelease.scala]: rnacentralrelease.scala.md
[main/scala/csvUtils.scala]: csvUtils.scala.md