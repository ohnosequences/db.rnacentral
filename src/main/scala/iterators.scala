package ohnosequences.db.rnacentral

case object iterators {

  def right[A, B]: Iterator[A + B] => Iterator[B] =
    abs => abs.collect({ case Right(z) => z: B })

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
