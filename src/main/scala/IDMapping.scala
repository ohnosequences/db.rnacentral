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

  val rows: RNACentralData => Iterator[ParsingError.MalformedRow + Row] =
    data => (io tsv data.idMapping) map rowFrom

  /**
    * Tries to parse each line of the ID mappings from a RNACentralData
    *
    * For each line in the file, it returns either a malformed row error, an
    * undefined field error or the correctly parsed entry annotation.
    * In the last two cases, an RNA ID is correctly parsed, so some information
    * can still be retrieved even if some field is undefined.
    */
  val entryAnnotationsOrErrors: RNACentralData => Iterator[
    ParsingError.MalformedRow + (ParsingError.UndefinedField + EntryAnnotation)
  ] =
    rows(_).map {
      case Left(err) => Left(err)
      case Right(row) =>
        entryAnnotationFrom(row) match {
          case Left(err)         => Right(Left(err))
          case Right(entryAnnot) => Right(Right(entryAnnot))
        }
    }

  /**
    * Tries to parse the ID mappings from a RNACentralData and group all
    * annotations under the same RNA ID
    *
    * The file to parse contains an entry annotation per line, with the entry ID
    * as one of the fields in the line. The same entry can have several
    * annotations in the file, each of them in a different, but adjacent, line.
    * This function groups all of these adjacent lines that correspond to the
    * same entry.
    *
    * All adjacent malformed rows where no ID can be identified are also grouped.
    *
    * @return an Iterator that yields either a group of adjacent malformed rows
    * or a tuple with:
    *   1. A RNA ID.
    *   2. A set containing the result of parsing adjacent lines that correspond
    *   to the RNA ID, each element being either a malformed entry (because of
    *   undefined fields other than the RNA ID) or a correctly parsed entry
    *   annotation.
    */
  val entryAnnotationsByRNAIDOrErrors: RNACentralData => Iterator[
    Set[ParsingError.MalformedRow] +
      (RNAID, Set[ParsingError.UndefinedField + EntryAnnotation])
  ] =
    data =>
      iterators.segmentsFrom({
        e: ParsingError.MalformedRow + (ParsingError.UndefinedField + EntryAnnotation) =>
          e match {
            case Left(z)          => None
            case Right(Left(uf))  => Some(uf.id)
            case Right(Right(ea)) => Some(ea.rnaID)
          }
      })(entryAnnotationsOrErrors(data)) map {
        case (None, xs) =>
          Left((xs collect { case Left(z) => z }).toSet)
        case (Some(id), xs) =>
          Right((id, (xs collect { case Right(z) => z }).toSet))
    }

  /**
    * Parse the ID mappings from a RNACentralData and builds both a set
    * of malformed rows and a map linking each different RNA ID to a set of its
    * corresponding parsed entries, either an error because of undefined fields
    * or the correctly parsed entry.
    *
    * See [[entryAnnotationsByRNAIDOrErrors]] for more information on the
    * parsing strategy.
    */
  val entryAnnotationsByRNAIDOrErrorsMap: RNACentralData => (
      Set[ParsingError.MalformedRow],
      Map[RNAID, Set[ParsingError.UndefinedField + EntryAnnotation]]
  ) =
    entryAnnotationsByRNAIDOrErrors(_).foldLeft(
      (Set.empty[ParsingError.MalformedRow],
       Map.empty[RNAID, Set[ParsingError.UndefinedField + EntryAnnotation]])
    ) {
      case ((malformedRows, accMap), possibleEntry) =>
        possibleEntry match {
          case Left(malformedRow) => (malformedRows ++ malformedRow, accMap)
          case Right((id, set))   => (malformedRows, accMap + (id -> set))
        }
    }

  val entryAnnotations
    : Iterator[Row] => Iterator[ParsingError.UndefinedField + EntryAnnotation] =
    _ map entryAnnotationFrom

  val entryAnnotationsByRNAID
    : Iterator[EntryAnnotation] => Iterator[(RNAID, Set[EntryAnnotation])] =
    xs =>
      iterators
        .segmentsFrom((_: EntryAnnotation).rnaID)(xs)
        .map { case (id, zs) => (id, zs.toSet) }

  sealed abstract class ParsingError
  case object ParsingError {

    final case class MalformedRow(val fields: Seq[String]) extends ParsingError

    sealed abstract class UndefinedField extends ParsingError { val id: RNAID }

    case object UndefinedField {

      final case class UndefinedDatabase(val id: RNAID, val name: String)
          extends UndefinedField
      final case class UndefinedRNAType(val id: RNAID, val name: String)
          extends UndefinedField
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  val rowFrom: Seq[String] => ParsingError.MalformedRow + Row = {
    case Seq(f1, f2, f3, f4, f5, f6) => Right { (f1, f2, f3, f4.toInt, f5, f6) }
    case other                       => Left(ParsingError.MalformedRow(other))
  }

  private val databaseEntryFrom: (String, String) => Option[DatabaseEntry] = {
    case (dbName, id) =>
      (Database from dbName) map { DatabaseEntry(_, id) }
  }

  val entryAnnotationFrom
    : Row => (ParsingError.UndefinedField + EntryAnnotation) = {
    case (id, dbName, dbID, taxID, rnaType, geneName) =>
      databaseEntryFrom(dbName, dbID)
        .fold[ParsingError.UndefinedField + EntryAnnotation](
          Left(ParsingError.UndefinedField.UndefinedDatabase(id, dbName))
        ) { dbEntry =>
          (RNAType from rnaType)
            .fold[ParsingError.UndefinedField + EntryAnnotation](
              Left(ParsingError.UndefinedField.UndefinedRNAType(id, rnaType))
            ) { rna =>
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
