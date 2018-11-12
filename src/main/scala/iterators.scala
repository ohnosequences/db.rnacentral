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
  def segmentsFrom[V, X]: (V => X) => (Iterator[V] => Iterator[(X, Seq[V])]) =
    f =>
      vs =>
        new Iterator[(X, Seq[V])] {

          private[this] val rest: BufferedIterator[V] =
            vs.buffered

          @annotation.tailrec
          private def segment_rec(fv: X,
                                  acc: collection.mutable.Buffer[V]): Seq[V] =
            if (rest.hasNext && f(rest.head) == fv)
              segment_rec(fv, acc += rest.next)
            else
              acc.toSeq

          private def segment: Seq[V] =
            segment_rec(f(rest.head), new collection.mutable.ArrayBuffer[V]())

          final def hasNext: Boolean =
            rest.hasNext

          final def next: (X, Seq[V]) =
            f(rest.head) -> segment
    }
}
