package ohnosequences.api

package object rnacentral {

  type +[A,B]   = Either[A,B]
  type RNAID    = String
  type TaxonID  = Int

  type RNAIDAndEntryAnnotation =
    (RNAID, EntryAnnotation)

  type SequenceWithAnnotation =
    (String, SequenceAnnotation)

  type RNAIDAndSequenceData =
    (RNAID, String, Set[SequenceAnnotation])

  type Grouped[X] =
    (RNAID, Set[X])

  type GroupedBy[X,Y] =
    (Y, Set[X])
}