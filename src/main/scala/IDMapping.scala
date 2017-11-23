package ohnosequences.api.rnacentral

case object IDMapping {

  // URS0000000023	ENA	JX826989.1:296..347:tRNA	1255168	tRNA	trnL
  type Row = (String, String, String, Int, String, String)

  val rows: RNACentralData => Iterator[ParsingError + Row] =
    data =>
      (io tsv data.idMapping) map rowFrom

  val entryAnnotations: Iterator[Row] => Iterator[ParsingError + (RNAID, EntryAnnotation)] =
    _ map entryAnnotationFrom

  sealed abstract class ParsingError
  case object ParsingError {

    case object generic                               extends ParsingError
    case class UndefinedDatabase(val name: String)    extends ParsingError
    case class UndefinedRNAType(val name: String)     extends ParsingError
    case class MalformedRow(val fields: Seq[String])  extends ParsingError
  }

  //////////////////////////////////////////////////////////////////////////////

  val rowFrom: Seq[String] => ParsingError + Row = {
    case Seq(f1,f2,f3,f4,f5,f6) => Right { (f1,f2,f3,f4.toInt,f5,f6) }
    case other                  => Left(ParsingError.MalformedRow(other))
  }
  
  private val databaseEntryFrom: (String, String) => Option[DatabaseEntry] = {
    case (dbName, id) =>
      (Database from dbName) map { DatabaseEntry(_, id) }
  }
  
  val entryAnnotationFrom: Row => (ParsingError + (RNAID, EntryAnnotation)) = {
    case (id, dbName, dbID, taxID, rnaType, geneName) =>
      databaseEntryFrom(dbName, dbID)
        .fold[ParsingError + (String, EntryAnnotation)](Left(ParsingError.UndefinedDatabase(dbName))) { dbEntry =>
          (RNAType from rnaType)
            .fold[ParsingError + (String, EntryAnnotation)](Left(ParsingError.UndefinedRNAType(rnaType))) { rna =>
              Right(
                id -> EntryAnnotation(
                  ncbiTaxonomyID  = taxID   ,
                  databaseEntry   = dbEntry ,
                  rnaType         = rna     ,
                  geneName        = 
                    if(geneName.isEmpty) None else Some(geneName)
                )
              )
          }
        }
  }
}
