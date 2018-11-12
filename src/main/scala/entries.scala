package ohnosequences.db.rnacentral

case object entries {

  /**
    * Parse all the information from a [[RNACentralData]]
    *
    * For the ID mappings, parse and build both a set of malformed rows and a
    * map linking each different RNA ID to a set of its  corresponding parsed
    * entries, either an error because of undefined fields or the correctly
    * parsed entry.
    *
    * For the sequences, group all of the adjacent entries that refer to the
    * same [[RNAID]] with all of its annotations. Then, look for each [[RNAID]]
    * in the ID mappings map built before, building an iterator whose elements
    * are either a set of errors or a well-formed [[Entry]]. The errors can be
    * caused by an entry that has no matching in the ID mapping map or by an
    * entry that has no annotations.
    *
    * @note For more information on the parsing strategy, see
    * [[IDMapping.entryAnnotationsByRNAIDOrErrorsMap]] and
    * [[sequences.parseFrom]]
    *
    * @return a tuple, where the first element is a set of all the malformed
    * rows in the ID mappings file, while the second is an iterator of either an
    * error (in the matching or the annotations) or a well-formed [[Entry]].
    *
    */
  val entriesFrom
    : RNACentralData => (Set[Error], Iterator[Set[Error] + Entry]) =
    data => {
      val (malformedRows, mappings) =
        IDMapping entryAnnotationsByRNAIDOrErrorsMap data

      (
        malformedRows map Error.IDMappingError,
        (sequences parseFrom data).map {
          case (x @ RNASequence(id1, _), seqAnnots) =>
            mappings.get(id1) match {
              case None =>
                Left(Set(Error.NoMatchingIDMapping(id1)))
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
      )
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

    /** The ID from the sequence file is not found in the ID mappings file */
    final case class NoMatchingIDMapping(
        val sequenceID: RNAID
    ) extends Error
  }
}
