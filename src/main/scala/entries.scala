package ohnosequences.api.rnacentral

case object entries {

  val entriesFrom: RNACentralData => Iterator[Set[Error] + Entry] =
    data =>
      (
        (sequences parseFrom data) zip 
        (IDMapping entryAnnotationsByRNAIDOrErrors data)
      )
      .map {

        case (_, Left(errs)) => 
          Left(errs map Error.IDMappingError)

        case ((x @ RNASequence(id1, seq), seqAnnots), Right((id2, annots)) ) =>
          if(id1 == id2)
            Right(
              Entry(
                rnaSequence         = x         ,
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