package ohnosequences.db.rnacentral.test

import ohnosequences.db.rnacentral
import ohnosequences.db.rnacentral._
import java.net.URL
import ohnosequences.test.ReleaseOnlyTest
import org.scalatest.FunSuite
import ohnosequences.files.{gzip, remote, tar}
import java.io.File

class MirrorInS3 extends FunSuite {

  private def mirrorVersion(version: Version): Unit = {
    import rnacentral.data.input

    // ID Mapping
    assertResult(Right(rnacentral.data.idMappingTSV(version))) {
      remote
        .download(
          new URL(input.idMappingTSVGZURL(version)),
          data.idMappingGZLocalFile(version)
        )
        .flatMap { file =>
          val tmpFile = File.createTempFile(file.getName, "uncompressed")
          tmpFile.deleteOnExit

          gzip.uncompress(file, tmpFile).flatMap { uncompressed =>
            tar.extract(uncompressed, data.localFolder(version)).flatMap { _ =>
              paranoidPutFile(
                data.idMappingLocalFile(version),
                rnacentral.data.idMappingTSV(version)
              )
            }
          }
        }
    }

    // FASTA
    assertResult(Right(rnacentral.data.speciesSpecificFASTA(version))) {
      remote
        .download(
          new URL(input.speciesSpecificFASTAGZURL(version)),
          data.fastaGZLocalFile(version)
        )
        .flatMap { file =>
          val tmpFile = File.createTempFile(file.getName, "uncompressed")
          tmpFile.deleteOnExit

          gzip.uncompress(file, tmpFile).flatMap { uncompressed =>
            tar.extract(uncompressed, data.localFolder(version)).flatMap { _ =>
              paranoidPutFile(
                data.fastaLocalFile(version),
                rnacentral.data.speciesSpecificFASTA(version)
              )
            }
          }
        }
    }
  }

  private def versionExistsInS3(version: Version): Boolean = {
    val objs = rnacentral.data everything version

    println(s"Checking ${version} data:")
    println(s"  ${objs}")

    objs forall { id =>
      s3Client.doesObjectExist(id.getBucket, id.getKey)
    }
  }

  private def printlnColor(color: String)(str: String): Unit =
    println(color + str + Console.RESET)

  private def printlnYellow = printlnColor(Console.YELLOW)(_)

  test("Check all releases - Never fails, just informative") {
    val notInS3 = Version.all filter { !versionExistsInS3(_) }

    if (!notInS3.isEmpty) {
      printlnYellow(
        "The next execution of release-only tests may incur in transfers to S3, as the following versions are not mirrored:"
      )
      notInS3 foreach { v =>
        printlnYellow(s"  * Version $v")
      }
    }
  }

  test("Mirror all releases", ReleaseOnlyTest) {
    Version.all foreach { version =>
      if (!versionExistsInS3(version)) {
        println(s"  Mirroring $version data...")

        data cleanLocalFolder version
        mirrorVersion(version)

        println(s"  $version data mirrored.")
      }
    }
  }
}
