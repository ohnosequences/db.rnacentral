package ohnosequences.db.rnacentral

import ohnosequences.fastarious._, fasta._

case object sequences {

  case object header {

    lazy val rnaID: Header => RNAID =
      rnaIDAndTaxonomyID andThen { _._1 }

    lazy val taxonomyID: Header => TaxonID =
      rnaIDAndTaxonomyID andThen { _._2 }

    lazy val rnaIDAndTaxonomyID: Header => (RNAID, TaxonID) =
      header => {

        val (p, s) =
          header.id.span(_ != '_')

        (p, s.drop(1).toInt)
      }

    lazy val sequenceAnnotation: Header => SequenceAnnotation =
      header =>
        SequenceAnnotation(
          rnaID = rnaID(header),
          ncbiTaxonomyID = taxonomyID(header),
          description = header.description
      )
  }

  case object fasta {

    val rnaID: FASTA => RNAID = { (_: FASTA).header } andThen header.rnaID

    val taxonomyID
      : FASTA => TaxonID = { (_: FASTA).header } andThen header.taxonomyID

    val sequenceAnnotation
      : FASTA => SequenceAnnotation = { (_: FASTA).header } andThen header.sequenceAnnotation

    val sequenceWithAnnotation: FASTA => SequenceWithAnnotation =
      fa => fa.sequence.letters -> sequenceAnnotation(fa)

    val rnaSequence: FASTA => RNASequence =
      fa => RNASequence(rnaID(fa), fa.sequence.letters)

    val grouped
      : Iterator[FASTA] => Iterator[SequenceAnnotation GroupedBy RNASequence] =
      fas =>
        iterators.segmentsFrom(rnaSequence)(fas) map {
          case (k, xs) => k -> xs.map(sequenceAnnotation).toSet
      }
  }

  lazy val parseFrom
    : RNACentralData => Iterator[SequenceAnnotation GroupedBy RNASequence] =
    fastaFrom andThen fasta.grouped

  val seqDataToFASTAs: RNAIDAndSequenceData => Set[FASTA] = {
    case (id, sequence, annotations) =>
      val seq =
        Sequence(sequence)

      val headers =
        annotations.map { a =>
          Header(s"${id}_${a.ncbiTaxonomyID} ${a.description}")
        }

      headers map { FASTA(_, seq) }
  }

  val seqDataToFASTA: RNAIDAndSequenceData => FASTA = {
    case (id, sequence, annotations) =>
      val descriptions =
        annotations.map(_.description).mkString(" | ")

      FASTA(
        Header(s"${id} ${descriptions}"),
        Sequence(sequence)
      )
  }

  lazy val fastaFrom: RNACentralData => Iterator[FASTA] = {
    (_: RNACentralData).speciesSpecificFasta
  } andThen
    io.lines andThen { _.buffered.parseFasta }

  val fastaByRNAID: RNACentralData => Iterator[(RNAID, Seq[FASTA])] =
    fastaFrom andThen (iterators segmentsFrom fasta.rnaID)

  val rnaIDAndSequenceDataFrom
    : Iterator[(RNAID, Seq[FASTA])] => Iterator[RNAIDAndSequenceData] =
    _ map {
      case (id, fas) =>
        (
          id, {
            @java.lang.SuppressWarnings(
              Array("org.wartremover.warts.TraversableOps"))
            val x = fas.head.sequence.letters;
            x
          },
          (fas map fasta.sequenceAnnotation).toSet
        )
    }

  val sequenceAnnotationsAndSequence
    : RNACentralData => Iterator[RNAIDAndSequenceData] =
    fastaByRNAID andThen rnaIDAndSequenceDataFrom
}
