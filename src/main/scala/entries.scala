package ohnosequences.db.rnacentral

case object entries {

  val entriesFrom: RNACentralData => Iterator[Set[Error] + Entry] =
    data => {
      val (malformedRows, mappings) =
        IDMapping entryAnnotationsByRNAIDOrErrorsMap data

      (sequences parseFrom data).map {
        case (x @ RNASequence(id1, _), seqAnnots) =>
          mappings.get(id1) match {
            case None =>
              Left(malformedRows map Error.IDMappingError)
            case Some(set) =>
              val entryAnnotations = set collect { case Right(ea) => ea }
              if (entryAnnotations.isEmpty)
                Left(
                  set collect { case Left(err) => err } map Error.IDMappingError
                )
              else
                Right(
                  Entry(
                    rnaSequence = x,
                    sequenceAnnotations = seqAnnots,
                    entryAnnotations = entryAnnotations
                  )
                )
          }
      }
    }

  sealed abstract class Error
  case object Error {

    final case class DifferentRNAIDs(
        val sequenceID: RNAID,
        val mappingID: RNAID
    ) extends Error

    final case class IDMappingError(
        val error: IDMapping.ParsingError
    ) extends Error
  }
}
