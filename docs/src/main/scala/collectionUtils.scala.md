
```scala
package era7bio.db
```

This object contains some generic collections ops that can be useful for filtering

```scala
case object collectionUtils {

  implicit class StreamOp[T](val s: Stream[T]) extends AnyVal {
```

Similar to .groupBy, by lazier: computes only one group at a time
assuming that groups are not mixed

```scala
    def group[K](key: T => K): Stream[(K, Stream[T])] = {
      if (s.isEmpty) Stream()
      else {
        val k = key(s.head)
        val (prefix, suffix) = s.span { key(_) == k }
        (k -> prefix) #:: suffix.group(key)
      }
    }
  }


  implicit class MapOp[K, V](val m: Map[K, Iterable[V]]) extends AnyVal {
```

From Map[K, Seq[V]] to Map[V, Seq[K]],
applying given function (`identity` by default)


```scala
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

```




[main/scala/blastDB.scala]: blastDB.scala.md
[main/scala/collectionUtils.scala]: collectionUtils.scala.md
[main/scala/csvUtils.scala]: csvUtils.scala.md
[main/scala/filterData.scala]: filterData.scala.md
[main/scala/rnacentral/compats.scala]: rnacentral/compats.scala.md
[main/scala/rnacentral/rnaCentral.scala]: rnacentral/rnaCentral.scala.md
[test/scala/runBundles.scala]: ../../test/scala/runBundles.scala.md