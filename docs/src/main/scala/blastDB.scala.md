
```scala
package era7bio.db

import ohnosequences.cosas._, types._, klists._
import ohnosequences.statika._
import ohnosequences.blast.api._
import ohnosequences.awstools.s3._

import ohnosequencesBundles.statika.Blast

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._


case object blastBundle extends Blast("2.2.31")
```

This bundle takes source data from the given place, generates BlastDB and
uploads it to S3. It _does not_ do any filtering, it should be handled by
other bundles which can be listed as dependencies.


```scala
abstract class GenerateBlastDB(
  val dbType: BlastDBType,
  val dbName: String,
  val sourceFastaS3: S3Object,
  val outputS3Prefix: S3Folder // where the generated files will be uploaded
)(deps: AnyBundle*) extends Bundle(blastBundle +: deps.toSeq: _*) {

  lazy val sources = file"sources/"
  lazy val outputs = file"outputs/"

  lazy val sourceFastaFile: File = sources / s"${dbName}.fasta"

  def instructions: AnyInstructions = {

    val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

    LazyTry {
      println(s"""Downloading the sources...
        |fasta: ${sourceFastaS3}
        |""".stripMargin)

      transferManager.download(
        sourceFastaS3.bucket, sourceFastaS3.key,
        sourceFastaFile.toJava
      ).waitForCompletion

      println("Generating BLAST DB...")
    } -&-
    seqToInstructions(
      makeblastdb(
        argumentValues =
          in(sourceFastaFile) ::
          input_type(DBInputType.fasta) ::
          dbtype(dbType) ::
          *[AnyDenotation],
        optionValues =
          title(dbName) ::
          *[AnyDenotation]
      ).toSeq
    ) -&-
    LazyTry {
      println("Uploading the DB...")

      // Moving blast DB files to the outputs/ folder
      outputs.createDirectory()
      sources.list
        .filter { _.name.startsWith(sourceFastaFile.name + ".") }
        .foreach { f => f.moveTo(outputs / f.name) }

      // Uploading outputs
      transferManager.uploadDirectory(
        outputS3Prefix.bucket, outputS3Prefix.key,
        outputs.toJava,
        false // includeSubdirectories
      ).waitForCompletion
    } -&-
    say(s"The database is uploaded to [${outputS3Prefix}]")
  }

}

```




[main/scala/blastDB.scala]: blastDB.scala.md
[main/scala/collectionUtils.scala]: collectionUtils.scala.md
[main/scala/csvUtils.scala]: csvUtils.scala.md
[main/scala/filterData.scala]: filterData.scala.md
[main/scala/rnacentral/compats.scala]: rnacentral/compats.scala.md
[main/scala/rnacentral/rnaCentral.scala]: rnacentral/rnaCentral.scala.md
[test/scala/runBundles.scala]: ../../test/scala/runBundles.scala.md