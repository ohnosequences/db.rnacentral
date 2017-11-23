package ohnosequences.api.rnacentral

package object test {

  def allRight[X,Y]: Iterator[X + Y] => Boolean =
    _.forall {
      case Left(_)  => false
      case Right(_) => true
    }
}