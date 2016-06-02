
```scala
package era7bio.db

import ohnosequences.cosas._, types._, klists._
import ohnosequences.statika._
import ohnosequences.awstools.s3._
import ohnosequences.fastarious.fasta._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import com.github.tototoshi.csv._
import better.files._
```

Each filtering bundle is defined by its input data, two output S3 folders and the filtering method.
It downloads the sources, filters and uploads two kinds of results: accepted and rejected data.
If you need to chain different filtering steps, just do it through bundle dependencies.


```scala
abstract class FilterData(
  sourceTableS3: S3Object,
  sourceFastaS3: S3Object,
  outputS3Prefix: S3Folder
)(deps: AnyBundle*) extends Bundle(deps.toSeq: _*) {
```

Names of the source/accepted/rejected files are the same as of the input S3 objects

```scala
  final lazy val tableName: String = sourceTableS3.key.split('/').last
  final lazy val fastaName: String = sourceFastaS3.key.split('/').last
```

Each folder has two files inside
Source folder provides ways to read the table and stream FASTA

```scala
  case object source { folder =>
    lazy val file: File = file"source".createDirectories()

    case object table {
      lazy val file = folder.file / tableName
      lazy val reader = CSVReader.open(this.file.toJava)(csvUtils.tsvFormat)
    }

    case object fasta {
      lazy val file = folder.file / fastaName
      lazy val stream: Stream[FASTA.Value] = parseFastaDropErrors(this.file.lines).toStream
    }
  }
```

Output folders know how to write to the files and where they will be uploaded

```scala
  case class outputFolder(name: String) { folder =>
    lazy val file: File   = File(name).createDirectories()
    lazy val s3: S3Folder = outputS3Prefix / folder.name /

    case object table {
      lazy val file: File = (folder.file / tableName).createIfNotExists()
      lazy val writer = CSVWriter.open(this.file.toJava, append = true)(csvUtils.tsvFormat)
      lazy val s3: S3Object = folder.s3 / tableName
    }

    case object fasta {
      lazy val file: File = (folder.file / fastaName).createIfNotExists()
      lazy val s3: S3Object = folder.s3 / fastaName
    }
  }

  lazy val accepted = outputFolder("accepted")
  lazy val rejected = outputFolder("rejected")
```

Implementing this method you define the filter.
It should refer to the folders/files defined above.

```scala
  def filterData(): Unit


  def instructions: AnyInstructions = {

    val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

    LazyTry {
      println(s"""Downloading the sources...
        |table: ${sourceTableS3}
        |fasta: ${sourceFastaS3}
        |""".stripMargin)

      transferManager.download(
        sourceTableS3.bucket, sourceTableS3.key,
        source.table.file.toJava
      ).waitForCompletion

      transferManager.download(
        sourceFastaS3.bucket, sourceFastaS3.key,
        source.fasta.file.toJava
      ).waitForCompletion
    } -&-
    LazyTry {
      println("Filtering the data...")

      filterData()

      source.table.reader.close()
      accepted.table.writer.close()
      rejected.table.writer.close()
    } -&-
    LazyTry {
      println("Uploading the results...")

      transferManager.uploadDirectory(
        accepted.s3.bucket, accepted.s3.key,
        accepted.file.toJava,
        false // don't includeSubdirectories
      ).waitForCompletion

      transferManager.uploadDirectory(
        rejected.s3.bucket, rejected.s3.key,
        rejected.file.toJava,
        false // don't includeSubdirectories
      ).waitForCompletion
    } -&-
    say(s"Filtered data is uploaded to [${accepted.s3}] and [${rejected.s3}]")
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