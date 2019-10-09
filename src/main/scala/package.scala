package ohnosequences.db

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import ohnosequences.dna._

package object rnacentral {

  type URL            = java.net.URL
  type rec            = annotation.tailrec
  type +[A, B]        = Either[A, B]
  type RNAID          = Long
  type TaxID          = Int
  type RNAID2Headers  = Long2ObjectOpenHashMap[Array[Header]]
  type RNAID2Mappings = Long2ObjectOpenHashMap[Array[Mapping]]
  type RNAID2Sequence = Long2ObjectOpenHashMap[DNA]

  implicit final class stringExt(val x: String) extends AnyVal {

    def tsv: String =
      s"${x}.tsv"

    def gz: String =
      s"${x}.gz"

    def fasta: String =
      s"${x}.fasta"

    def /(other: String): String =
      s"${x}/${other}"
  }

  implicit final class urlExt(val x: URL) extends AnyVal {

    def /(y: String): URL =
      ???
  }
}
