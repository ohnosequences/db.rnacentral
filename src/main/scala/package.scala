package ohnosequences.db

package object rnacentral {

  type rec     = annotation.tailrec
  type +[A, B] = Either[A, B]
  type RNAID   = Long
  type TaxID   = Int
  type TaxonID = Int

  type RNAIDAndEntryAnnotation =
    (RNAID, EntryAnnotation)

  type SequenceWithAnnotation =
    (String, SequenceAnnotation)

  type RNAIDAndSequenceData =
    (RNAID, String, Set[SequenceAnnotation])

  type Grouped[X] =
    (RNAID, Set[X])

  type GroupedBy[X, Y] =
    (Y, Set[X])
}
