package ohnosequences.api.rnacentral

case object entries {

  val entriesFrom: RNACentralData => Iterator[Set[Error] + Entry] =
    data =>
      (
        (sequences sequenceAnnotationsAndSequence data) zip 
        (IDMapping entryAnnotationsByRNAIDOrErrors data)
      )
      .map {

        case (_, Left(errs)) => 
          Left(errs map Error.IDMappingError)

        case ( (id1, seq, seqAnnots), Right((id2, annots)) ) =>
          if(id1 == id2)
            Right(
              Entry(
                rnaID               = id1       ,
                sequence            = seq       ,
                sequenceAnnotations = seqAnnots ,
                entryAnnotations    = annots    
              )
            )
          else
            Left(Set(Error.DifferentRNAIDs(id1,id2)))
      }  
    
  sealed abstract class Error
  case object Error {

    case class DifferentRNAIDs(
      val sequenceID: RNAID,
      val mappingID : RNAID
    )   extends Error

    case class IDMappingError(
      val error: IDMapping.ParsingError + IDMapping.ParsingError
    ) extends Error
  }
}