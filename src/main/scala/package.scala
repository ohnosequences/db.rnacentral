package ohnosequences.db

package object rnacentral {

  type rec     = annotation.tailrec
  type +[A, B] = Either[A, B]
  type RNAID   = Long
  type TaxID   = Int
}
