package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.db.rnacentral, rnacentral.{Version, s3Helpers}
import org.scalatest.EitherValues._

class Existence extends FunSuite {

  test("All supported versions exist") {
    Version.all foreach { v =>
      rnacentral.data.everything(v).foreach { obj =>
        val objectExistence = s3Helpers.objectExists(obj).right.value
        assert(
          objectExistence,
          s"- Version $v is not complete: object $obj does not exist."
        )
      }
    }
  }
}
