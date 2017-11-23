package ohnosequences.api

package object rnacentral {

  type +[A,B]   = Either[A,B]
  type RNAID    = String
  type TaxonID  = Int
}