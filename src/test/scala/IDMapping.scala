package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.test._
import ohnosequences.db.rnacentral._

class IDMapping extends FunSuite {

  test("parse all EntryAnnotations", ReleaseOnlyTest) {
    Version.all foreach { version =>
      def entries =
        IDMapping entryAnnotations (
          iterators right (
            IDMapping rows data.rnacentralData(version)
          )
        )

      assert { allRight(entries) }
    }

  }

  test("well-formed tsv", ReleaseOnlyTest) {
    Version.all foreach { version =>
      assert { allRight(IDMapping rows data.rnacentralData(version)) }
    }

  }
}
