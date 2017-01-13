package ohnosequences.db

/* This object contains some generic collections ops that can be useful for filtering */
case object collectionUtils {

  implicit class IteratorOps[V](val iterator: Iterator[V]) extends AnyVal {

    /* Similar to the Stream's .groupBy, but assuming that groups are contiguous. Another difference is that it returns the key corresponding to each group. */
    // NOTE: The original iterator should be discarded after calling this method
    def contiguousGroupBy[K](getKey: V => K): Iterator[(K, Seq[V])] = new Iterator[(K, Seq[V])] {
      /* The definition is very straightforward: we keep the `rest` of values and on each `.next()` call bite off the longest prefix with the same key */

      /* Buffered iterator allows to look ahead without removing the next element */
      private val rest: BufferedIterator[V] = iterator.buffered

      // NOTE: this is so simple, because of the contiguous grouping assumpltion
      def hasNext: Boolean = rest.hasNext

      def next(): (K, Seq[V]) = {
        val key = getKey(rest.head)

        key -> groupOf(key)
      }

      @annotation.tailrec
      private def groupOf_rec(key: K, acc: Seq[V]): Seq[V] = {
        if ( rest.hasNext && getKey(rest.head) == key )
          groupOf_rec(key, rest.next() +: acc)
        else acc
      }

      private def groupOf(key: K): Seq[V] = groupOf_rec(key, Seq())
    }
  }

  implicit class MapOp[K, V](val m: Map[K, Iterable[V]]) extends AnyVal {

    /* From Map[K, Seq[V]] to Map[V, Seq[K]],
       applying given function (`identity` by default)
    */
    def trans[FK, FV](f: ((K, V)) => (FK, FV)): Map[FV, Seq[FK]] =
      m.foldLeft(Map[FV, Seq[FK]]()) { case (acc, (k, vs)) =>

        vs.foldLeft(acc) { (accc, v) =>

          val (fk, fv) = f(k -> v)
          val fks = accc.get(fv).getOrElse( Seq() )

          accc.updated(fv, fk +: fks)
        }
      }

    def trans: Map[V, Seq[K]] = trans(identity[(K, V)])
  }

}
