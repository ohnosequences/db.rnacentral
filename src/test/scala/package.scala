package ohnosequences.db.rnacentral

package object test {

  type +[A, B] = Either[A, B]

  def allRight[X, Y]: Iterator[X + Y] => Boolean =
    _.forall {
      case Left(_)  => false
      case Right(_) => true
    }
}
