package ohnosequences.db.rnacentral

import ohnosequences.fastarious._, fasta._

case object sequences {

  case object header {

    /**
      * Parse the [[RNAID]] from a [[Header]]
      */
    lazy val rnaID: Header => RNAID =
      rnaIDAndTaxonomyID andThen { _._1 }

    /**
      * Parse the [[TaxonID]] from a [[Header]]
      */
    lazy val taxonomyID: Header => TaxonID =
      rnaIDAndTaxonomyID andThen { _._2 }

    /**
      * Parse the [[RNAID]] and [[TaxonID]] as a tuple from a [[Header]]
      */
    lazy val rnaIDAndTaxonomyID: Header => (RNAID, TaxonID) =
      header => {

        val (p, s) =
          header.id.span(_ != '_')

        (p, s.drop(1).toInt)
      }

    /**
      * Parse a [[SequenceAnnotation]] from a [[Header]]
      */
    lazy val sequenceAnnotation: Header => SequenceAnnotation =
      header =>
        SequenceAnnotation(
          rnaID = rnaID(header),
          ncbiTaxonomyID = taxonomyID(header),
          description = header.description
      )
  }

  case object fasta {

    /**
      * Parse the [[RNAID]] from the header of a [[FASTA]]
      */
    val rnaID: FASTA => RNAID = { (_: FASTA).header } andThen header.rnaID

    /**
      * Parse the [[TaxonID]] from the header of a [[FASTA]]
      */
    val taxonomyID
      : FASTA => TaxonID = { (_: FASTA).header } andThen header.taxonomyID

    /**
      * Parse a [[SequenceAnnotation]] from the header of a [[FASTA]]
      */
    val sequenceAnnotation
      : FASTA => SequenceAnnotation = { (_: FASTA).header } andThen header.sequenceAnnotation

    /**
      * Parse a tuple linking the sequence of a [[FASTA]] with its
      * [[sequenceAnnotation]].
      */
    val sequenceWithAnnotation: FASTA => SequenceWithAnnotation =
      fa => fa.sequence.letters -> sequenceAnnotation(fa)

    /**
      * Parse the [[RNASequence]] from a [[FASTA]]
      */
    val rnaSequence: FASTA => RNASequence =
      fa => RNASequence(rnaID(fa), fa.sequence.letters)

    /**
      * From an iterator of [[FASTA]]s, yield a tuple where the first element is
      * an [[RNASequence]] and the second a set of [[SequenceAnnotation]]s that
      * are all assigned to that [[RNASequence]].
      */
    val grouped
      : Iterator[FASTA] => Iterator[SequenceAnnotation GroupedBy RNASequence] =
      fas =>
        iterators.segmentsFrom(rnaSequence)(fas) map {
          case (k, xs) => k -> xs.map(sequenceAnnotation).toSet
      }
  }

  /**
    * Given an [[RNACentralData]], build an iterator that yields tuples where
    * first element is an [[RNASequence]] and the second a set of
    * [[SequenceAnnotation]]s that are all assigned to that [[RNASequence]].
    */
  lazy val parseFrom
    : RNACentralData => Iterator[SequenceAnnotation GroupedBy RNASequence] =
    fastaFrom andThen fasta.grouped

  /**
    * Given an [[RNAIDAndSequenceData]], build a set of [[FASTA]]s that contain
    * all the same sequence with header one of the annotations of the input data.
    */
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

  /**
    * Given an [[RNAIDAndSequenceData]], build a unique [[FASTA]] that contains
    * the concatenation of all the annotations (separated with " | ") as header
    * and the original sequence.
    */
  val seqDataToFASTA: RNAIDAndSequenceData => FASTA = {
    case (id, sequence, annotations) =>
      val descriptions =
        annotations.map(_.description).mkString(" | ")

      FASTA(
        Header(s"${id} ${descriptions}"),
        Sequence(sequence)
      )
  }

  /**
    * Parse the FASTA from an [[RNACentralData]]
    * @type {[type]}
    */
  lazy val fastaFrom: RNACentralData => Iterator[FASTA] = {
    (_: RNACentralData).speciesSpecificFasta
  } andThen
    io.lines andThen { _.buffered.parseFasta }

  /**
    * Parse the FASTA from an [[RNACentralData]] and group them by equal
    * [[RNAID]]
    */
  val fastaByRNAID: RNACentralData => Iterator[(RNAID, Seq[FASTA])] =
    fastaFrom andThen (iterators segmentsFrom fasta.rnaID)

  /**
    * Convert an RNAID and a collection of FASTAs into a
    * [[RNAIDAndSequenceData]], with the RNAID, the sequence of the first FASTA
    * and the set of all the annotations in the collection of FASTAs.
    */
  val rnaIDAndSequenceDataFrom
    : Iterator[(RNAID, Seq[FASTA])] => Iterator[RNAIDAndSequenceData] =
    _ map {
      case (id, fas) =>
        (
          id, {
            @java.lang.SuppressWarnings(
              Array("org.wartremover.warts.TraversableOps")
            )
            val x = fas.head.sequence.letters;
            x
          },
          (fas map fasta.sequenceAnnotation).toSet
        )
    }

  /**
    * Parse the FASTA from an [[RNACentralData]] and group them by equal
    * [[RNAID]], converting the groups into a [[RNAIDAndSequenceData]]
    * afterwards.
    */
  val sequenceAnnotationsAndSequence
    : RNACentralData => Iterator[RNAIDAndSequenceData] =
    fastaByRNAID andThen rnaIDAndSequenceDataFrom
}
