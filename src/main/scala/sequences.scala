package ohnosequences.api.rnacentral

import ohnosequences.fastarious._, fasta._

case object sequences {

  val fasta: RNACentralData => Iterator[FASTA] =
    { (_:RNACentralData).speciesSpecificFasta } andThen
    io.lines                                    andThen
    { _.buffered.parseFasta }

  val rnaIDFromFASTA: FASTA => RNAID =
    { (_:FASTA).header } andThen rnaIDFromHeader
  
  val taxonomyIDFromFASTA: FASTA => TaxonID =
    { (_:FASTA).header } andThen taxonomyIDFromHeader
    
  val sequenceAnnotationFromFASTA: FASTA => SequenceAnnotation =
    { (_:FASTA).header } andThen sequenceAnnotationFromHeader

  val fastaByRNAID: RNACentralData => Iterator[(RNAID, Seq[FASTA])] =
    fasta andThen (iterators segmentsFrom rnaIDFromFASTA)

  val sequenceAnnotationsAndSequence
    : RNACentralData => Iterator[(RNAID, String, Set[SequenceAnnotation])] =
      data => sequences.fastaByRNAID(data) map {
      case (id, fas) =>
        (
          id,
          {
            @java.lang.SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
            val x = fas.head.sequence.letters;
            x
          },
          (fas map sequenceAnnotationFromFASTA).toSet
        )
    }

  //////////////////////////////////////////////////////////////////////////

  lazy val rnaIDFromHeader: Header => RNAID =
    rnaIDAndTaxonomyIDFromHeader andThen { _._1 }

  lazy val taxonomyIDFromHeader: Header => TaxonID =
    rnaIDAndTaxonomyIDFromHeader andThen { _._2 }

  lazy val rnaIDAndTaxonomyIDFromHeader: Header => (RNAID, TaxonID) =
    header => {

      val (p,s) = 
        header.id.span(_!='_')

      (p, s.drop(1).toInt)
    }

  lazy val sequenceAnnotationFromHeader: Header => SequenceAnnotation =
    header => 
      SequenceAnnotation(
        ncbiTaxonomyID  = sequences.taxonomyIDFromHeader(header),
        description     = header.description
      )
}