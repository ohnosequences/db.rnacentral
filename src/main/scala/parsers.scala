package  ohnosequences.api.rnacentral

import java.io.File

case class RNACentralData(
  val idMapping             : File,
  val speciesSpecificFasta  : File
)

case object Entries {

  import scala.collection.JavaConverters._
  import com.github.tototoshi.csv._
  import java.nio.file.Files
  import java.io.File
  import ohnosequences.fastarious.fasta._
  import IDMappingFile._

  val entriesFrom: RNACentralData => Iterator[Entry] =
    data =>
      (
        sequenceAnnotationsAndSequence(sequences(data)) zip
        groupedAnnotations(annotations(rows(data))) 
      )
      .map { case ((id1, seq, seqAnnots),(id2,annots)) =>
        Entry(
          rnaID               = id1       ,
          sequence            = seq       ,
          sequenceAnnotations = seqAnnots ,
          entryAnnotations    = annots    
        )
      }
  
  val lines: File => Iterator[String] =
    file => Files.lines(file.toPath).iterator.asScala

  val sequences: RNACentralData => Iterator[FASTA] =
    { (_:RNACentralData).speciesSpecificFasta } andThen
    lines                                       andThen
    { _.buffered.parseFasta }

  val rnaIDFromFASTA: FASTA => RNAID =
    { (_:FASTA).header } andThen TaxonSpecificFASTA.rnaIDFrom

  val sequenceAnnotationsAndSequence
    : Iterator[FASTA] => Iterator[(RNAID, String, Set[SequenceAnnotation])] =
    xs =>
      iterators.segmentsFrom(rnaIDFromFASTA)(xs) map {
        case (id, fas) =>
          (
            id,
            {
              @java.lang.SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
              val x = fas.head.sequence.letters;
              x
            },
            fas
              .map(
                { (_:FASTA).header } andThen
                TaxonSpecificFASTA.sequenceAnnotationFrom
              )
              .toSet
          )
      }

  val rows: RNACentralData => Iterator[ParsingError + Row] =
    data =>
      (CSVReader.open(data.idMapping)(io.format)).iterator map rowFrom

  val annotations: Iterator[ParsingError + Row] => Iterator[(RNAID, EntryAnnotation)] = 
    _
      .collect { case Right(row) => entryAnnotationFrom(row) }
      .collect { case Right(x) => x }


  val groupedAnnotations: Iterator[(RNAID, EntryAnnotation)] => Iterator[(RNAID, Set[EntryAnnotation])] =
    xs =>
      iterators.segmentsFrom({ e: (RNAID, EntryAnnotation) => e._1 })(xs)
        .map { case (id, es) => (id, es.map(_._2).toSet) }
}

case object IDMappingFile {

  // URS0000000023	ENA	JX826989.1:296..347:tRNA	1255168	tRNA	trnL
  type Row = (String, String, String, Int, String, String)

  val rowFrom: Seq[String] => ParsingError + Row = {
    case Seq(f1,f2,f3,f4,f5,f6) => Right { (f1,f2,f3,f4.toInt,f5,f6) }
    case other                  => Left(ParsingError.MalformedRow(other))
  }

  val databaseEntryFrom: (String, String) => Option[DatabaseEntry] = {
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

  sealed abstract class ParsingError
  case object ParsingError {

    case object generic                               extends ParsingError
    case class UndefinedDatabase(val name: String)    extends ParsingError
    case class UndefinedRNAType(val name: String)     extends ParsingError
    case class MalformedRow(val fields: Seq[String])  extends ParsingError
  }
}

case object TaxonSpecificFASTA {

  import ohnosequences.fastarious._
  
  val rnaIDAndTaxonomyIDFrom: fasta.Header => (RNAID, TaxonID) =
    header => {

      val (p,s) = 
        header.id.span(_!='_')

      (p, s.drop(1).toInt)
    }
  
  val rnaIDFrom: fasta.Header => RNAID =
    rnaIDAndTaxonomyIDFrom andThen { _._1 }

  val taxonomyIDFrom: fasta.Header => TaxonID =
    rnaIDAndTaxonomyIDFrom andThen { _._2 }

  val sequenceAnnotationFrom: fasta.Header => SequenceAnnotation =
    header => 
      SequenceAnnotation(
        ncbiTaxonomyID  = taxonomyIDFrom(header),
        description     = header.description
      )
}