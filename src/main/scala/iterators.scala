package ohnosequences.db.rnacentral

case object iterators {

  /**
    * Discard all Left elements in the input Iterator.
    */
  def right[A, B]: Iterator[A + B] => Iterator[B] =
    abs => abs.collect({ case Right(z) => z: B })

  /**
    * Given an iterator of `V` elements, group them in an iterator of `(X,
    * Seq[V])` elements, where the elements of the sequence are adjacent
    * elements in the original iterator that are mapped to the same element `X`
    * by the passed function `V => X`
    */
  def segmentsFrom[V: reflect.ClassTag, X](
      f: V => X): (Iterator[V] => Iterator[(X, Array[V])]) =
    vs =>
      new Iterator[(X, Array[V])] {

        private[this] val rest: BufferedIterator[V] =
          vs.buffered

        @annotation.tailrec
        private def segment_rec(fv: X,
                                acc: collection.mutable.Buffer[V]): Array[V] =
          if (rest.hasNext && f(rest.head) == fv)
            segment_rec(fv, acc += rest.next)
          else
            acc.toArray

        private def segment: Array[V] =
          segment_rec(f(rest.head), new collection.mutable.ArrayBuffer[V]())

        final def hasNext: Boolean =
          rest.hasNext

        final def next: (X, Array[V]) =
          f(rest.head) -> segment
    }
}
