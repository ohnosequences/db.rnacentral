package ohnosequences.db.rnacentral

import java.net.URL
import ohnosequences.files.{gzip, remote, tar}
import java.io.File
import ohnosequences.s3.S3Object

case object release {

  private def mirrorVersion(
      version: Version,
      localFolder: File
  ): Error + Set[S3Object] = {

    def mirrorFile(
        url: URL,
        gzFile: File,
        file: File,
        s3Obj: S3Object
    ): Error + S3Object = {
      // Auxiliary file for uncompressing the .gz file
      val tmpFile = File.createTempFile(file.getName, "uncompressed")
      tmpFile.deleteOnExit

      // Try to download, uncompress and extract the file
      val maybeFile =
        remote
          .download(url, gzFile)
          .flatMap(file => gzip.uncompress(file, tmpFile))
          .flatMap(archive => tar.extract(archive, localFolder))
          .map(_ => file)

      // If everything worked as expected, try to upload the file to S3
      // Otherwise, adapt the File error to this package's Error.
      maybeFile match {
        case Left(err) => Left(Error.FileError(err))
        case Right(file) =>
          s3Helpers.paranoidPutFile(file, s3Obj) match {
            case Left(err) => Left(Error.S3Error(err))
            case Right(_)  => Right(s3Obj)
          }
      }
    }

    // IDMappings
    mirrorFile(
      url = new URL(data.input.idMappingTSVGZURL(version)),
      gzFile = data.local.idMappingGZFile(version, localFolder),
      file = data.local.idMappingFile(version, localFolder),
      s3Obj = data.idMappingTSV(version)
    ).flatMap { idMappingsS3 =>
      // FASTA
      mirrorFile(
        url = new URL(data.input.speciesSpecificFASTAGZURL(version)),
        gzFile = data.local.fastaGZFile(version, localFolder),
        file = data.local.fastaFile(version, localFolder),
        s3Obj = data.speciesSpecificFASTA(version)
      ).map { fastaS3 =>
        Set(idMappingsS3, fastaS3)
      }
    }
  }

  private def someObjectExists(version: Version): Option[S3Object] =
    data
      .everything(version)
      .find(
        obj => s3Helpers.objectExists(obj).fold(_ => true, identity)
      )

  def mirrorNewVersion(
      version: Version,
      localFolder: File
  ): Error + Set[S3Object] =
    someObjectExists(version).fold(mirrorVersion(version, localFolder)) { obj =>
      Left(Error.S3ObjectExists(obj))
    }
}
