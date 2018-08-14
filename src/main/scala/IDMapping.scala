package ohnosequences.db.rnacentral

case object IDMapping {

  // URS0000000023	ENA	JX826989.1:296..347:tRNA	1255168	tRNA	trnL
  type Row = (String, String, String, Int, String, String)

  val entryAnnotationToRow: RNAIDAndEntryAnnotation => Row = {
    case (id, a) =>
      (
        id,
        a.databaseEntry.database.name,
        a.databaseEntry.id,
        a.ncbiTaxonomyID,
        a.rnaType.name,
        a.geneName getOrElse ""
      )
  }

  val rows: RNACentralData => Iterator[ParsingError + Row] =
    data => (io tsv data.idMapping) map rowFrom

  val entryAnnotationsOrErrors: RNACentralData => Iterator[
    (ParsingError + ParsingError) + EntryAnnotation] =
    rows(_).map {
      case Left(err) => Left(Left(err))
      case Right(row) =>
        entryAnnotationFrom(row) match {
          case Left(err)         => Left(Right(err))
          case Right(entryAnnot) => Right(entryAnnot)
        }
    }

  val entryAnnotationsByRNAIDOrErrors: RNACentralData => Iterator[
    Set[ParsingError + ParsingError] + (RNAID, Set[EntryAnnotation])
  ] =
    data =>
      iterators.segmentsFrom({
        e: (ParsingError + ParsingError) + EntryAnnotation =>
          e match {
            case Left(z)   => None
            case Right(ea) => Some(ea.rnaID)
          }
      })(entryAnnotationsOrErrors(data)) map {
        case (None, xs) =>
          Left((xs collect { case Left(z) => z }).toSet)
        case (Some(id), xs) =>
          Right { (id, (xs collect { case Right(z) => z }).toSet) }
    }

  val entryAnnotations
    : Iterator[Row] => Iterator[ParsingError + EntryAnnotation] =
    _ map entryAnnotationFrom

  val entryAnnotationsByRNAID
    : Iterator[EntryAnnotation] => Iterator[(RNAID, Set[EntryAnnotation])] =
    xs =>
      iterators
        .segmentsFrom((_: EntryAnnotation).rnaID)(xs)
        .map { case (id, zs) => (id, zs.toSet) }

  sealed abstract class ParsingError
  case object ParsingError {

    case object generic                                    extends ParsingError
    final case class UndefinedDatabase(val name: String)   extends ParsingError
    final case class UndefinedRNAType(val name: String)    extends ParsingError
    final case class MalformedRow(val fields: Seq[String]) extends ParsingError
  }

  //////////////////////////////////////////////////////////////////////////////

  val rowFrom: Seq[String] => ParsingError + Row = {
    case Seq(f1, f2, f3, f4, f5, f6) => Right { (f1, f2, f3, f4.toInt, f5, f6) }
    case other                       => Left(ParsingError.MalformedRow(other))
  }

  private val databaseEntryFrom: (String, String) => Option[DatabaseEntry] = {
    case (dbName, id) =>
      (Database from dbName) map { DatabaseEntry(_, id) }
  }

  val entryAnnotationFrom: Row => (ParsingError + EntryAnnotation) = {
    case (id, dbName, dbID, taxID, rnaType, geneName) =>
      databaseEntryFrom(dbName, dbID)
        .fold[ParsingError + EntryAnnotation](
          Left(ParsingError.UndefinedDatabase(dbName))) { dbEntry =>
          (RNAType from rnaType)
            .fold[ParsingError + EntryAnnotation](
              Left(ParsingError.UndefinedRNAType(rnaType))) { rna =>
              Right(
                EntryAnnotation(
                  rnaID = id,
                  ncbiTaxonomyID = taxID,
                  databaseEntry = dbEntry,
                  rnaType = rna,
                  geneName = if (geneName.isEmpty) None else Some(geneName)
                )
              )
            }
        }
  }
}
