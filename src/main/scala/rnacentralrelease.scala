package era7bio.db

import ohnosequences.cosas._, types._, records._, klists._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._
import ohnosequences.statika._, aws._
import better.files._
import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._
import era7.defaults._, loquats._

/*
  ### RNAcentral sequences

  We have RNA sequences mirrored at S3, scoped by version.
*/
abstract class AnyRNAcentral(val version: String) {

  lazy val prefix = S3Object("resources.ohnosequences.com","")/"rnacentral"/version/

  val fastaFileName           : String = s"rnacentral.${version}.fasta"
  val id2taxafullFileName     : String = s"id2taxafull.${version}.tsv"
  val id2taxaFileName         : String = s"id2taxa.${version}.tsv"
  val id2taxafilteredFileName : String = s"id2taxa.filtered.${version}.tsv"

  lazy val fasta            : S3Object = prefix/fastaFileName
  lazy val id2taxafull      : S3Object = prefix/id2taxafullFileName
  lazy val id2taxa          : S3Object = prefix/id2taxaFileName
  lazy val id2taxafiltered  : S3Object = prefix/id2taxafilteredFileName
}

case object RNAcentral extends AnyRNAcentral("4.0") {

  case object id            extends Type[String]("id")
  case object db            extends Type[String]("db")
  case object external_id   extends Type[String]("external_id")
  case object tax_id        extends Type[String]("tax_id")

  case object Id2TaxaFull extends RecordType(
    id          :×:
    db          :×:
    external_id :×:
    tax_id      :×:
    |[AnyType]
  )

  case object Id2Taxa extends RecordType(
    id     :×:
    tax_id :×:
    |[AnyType]
  )
}

case object RNAcentralRelease extends Bundle() {

  val rnaCentral: RNAcentral.type = RNAcentral
  lazy val dataFolder = file"/media/ephemeral0"

  lazy val rnaCentralfastaFile  = dataFolder/rnaCentral.fastaFileName
  lazy val id2taxafullFile      = dataFolder/rnaCentral.id2taxafullFileName
  lazy val id2taxaFile          = dataFolder/rnaCentral.id2taxaFileName
  lazy val id2taxafilteredFile  = dataFolder/rnaCentral.id2taxafilteredFileName
  lazy val errLogFile           = dataFolder/s"${toString}.log"


  def instructions: AnyInstructions = LazyTry {

    val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

    // get the fasta file
    transferManager.download(
      rnaCentral.fasta.bucket, rnaCentral.fasta.key,
      rnaCentralfastaFile.toJava
    )
    .waitForCompletion

    // get the full id file
    transferManager.download(
      rnaCentral.id2taxafull.bucket, rnaCentral.id2taxafull.key,
      id2taxafullFile.toJava
    )
    .waitForCompletion

    // drop fields, write to another file
    fileWrangling.dropSomeFields(id2taxafullFile, id2taxaFile)

    fileWrangling.filterInactiveIds(
      rnaCentralfastaFile,
      id2taxaFile,
      id2taxafilteredFile,
      errLogFile
    )

    transferManager.upload(
      rnaCentral.id2taxafiltered.bucket, rnaCentral.id2taxafiltered.key,
      id2taxafilteredFile.toJava
    )
    .waitForCompletion

    // generate BLAST db
    generateBLASTdb.from(rnaCentralfastaFile)
  }



  case object compat extends Compatible(
      amznAMIEnv(
        AmazonLinuxAMI(Ireland, HVM, InstanceStore),
        javaHeap = 20 // in G
      ),
      RNAcentralRelease,
      generated.metadata.Rnacentraldb
    )

  def runTask(user: AWSUser): List[String] = {
    EC2.create(user.profile)
      .runInstances(
        amount = 1,
        compat.instanceSpecs(
          c3.x2large,
          user.keypair.name,
          Some(ec2Roles.projects.name)
        )
      ).map { inst =>

        val id = inst.getInstanceId
        println(s"Launched [${id}]")
        id
      }
  }
}

case object generateBLASTdb {

  import ohnosequences.blast.api._

  def from(fastaFile: File): Unit = {

    val mkdb = makeblastdb(
      argumentValues =
        in(fastaFile)                 ::
        input_type(DBInputType.fasta) ::
        dbtype(BlastDBType.nucl)      ::
        *[AnyDenotation],
      optionValues = makeblastdb.defaults.update(
          title(fastaFile.name) :: *[AnyDenotation]
        ).value
    )

    import sys.process._
    mkdb.toSeq.!!
  }
}

case object fileWrangling {

  def dropSomeFields(id2taxafullFile: File, id2TaxaFile: File): Unit = {

    import RNAcentral._

    // ugly I know. we need a csv lib
    csvUtils.rows(id2taxafullFile)(Id2TaxaFull.keys.types map typeLabel asList).foreach {
      row => Id2TaxaFull.parse(row) match {
        case Right(v)   => id2TaxaFile << s"${v.getV(id)}\t${v.getV(tax_id)}"
        case Left(err)  => println(err)
      }
    }
  }

  def filterInactiveIds(
    rnaCentralFastaFile : File,
    id2TaxaFile         : File,
    id2TaxaFiltered     : File,
    errLog              : File
  ): Unit = {

    // val errLog = file"filterInactiveIds.log"
    // val id2TaxaFiltered = file"id2taxa.filtered.tsv"
    import ohnosequences.fastarious.fasta._
    import RNAcentral._

    val fastas = parseFromLines(rnaCentralFastaFile.lines) map { map => FASTA.parse(map) }
    val allIds = csvUtils.rows(id2TaxaFile)(Id2Taxa.keys.types map typeLabel asList).map {
      row => Id2Taxa.parse(row) match { case Right(v) => v }
    }

    fastas.foreach {

      case Left(err) => errLog << err.toString
      case Right(fa) => {
        // advance ids
        val rest = allIds.dropWhile( _.getV(id) != fa.getV(header).id )

        if(rest.hasNext) {

          val gotit = rest.next
          id2TaxaFiltered << s"${gotit.getV(id)}\t${gotit.getV(tax_id)}"
        }
        else {
          errLog << s"${fa.getV(header)} not found in id2taxa file"
        }
      }
    }
  }
}
