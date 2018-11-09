package ohnosequences.db.rnacentral

import java.net.URL
import ohnosequences.files.{directory, gzip, remote}
import java.io.File
import ohnosequences.s3.S3Object

case object release {

  /**
    * Perform the actual mirror of RNACentral, overwriting if necessary
    *
    * For [[data.input.idMappingTSVGZURL]], [[data.idMappingTSV]] is uploaded to
    * S3. For [[data.input.speciesSpecificFASTAGZURL]],
    * [[data.speciesSpecificFASTA]] is uploaded to S3.
    *
    * The process to mirror each of those files is:
    *   1. Download the `.gz` file from [[data.input.releaseURL]]
    *   2. Uncompress to obtain the file
    *   4. Upload the file ([[data.input.idMappingTSV]] and
    *   [[data.input.speciesSpecificFASTA]] resp.) to the folder
    *   [[data.prefix]].
    *
    * @return an Error + Set[S3Object], with a Right(set) with all the mirrored
    * S3 objects if everything worked as expected or with a Left(error) if an
    * error occurred. Several things could go wrong in this process; namely:
    *   - The input files could not be downloaded
    *   - The input files could not be uncompressed
    *   - The upload process failed, either because you have no permissions to
    *   upload the objects or because some error occured during the upload
    *   itself.
    */
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
      // Try to download and uncompress the file
      val maybeFile =
        remote
          .download(url, gzFile)
          .flatMap(gzFile => gzip.uncompress(gzFile, file))

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

    directory
      .createDirectory(localFolder)
      .left
      .map(Error.FileError)
      .right
      .flatMap { localFolder =>
        // IDMappings
        mirrorFile(
          url = new URL(data.input.idMappingTSVGZURL(version)),
          gzFile = data.local.idMappingGZFile(localFolder),
          file = data.local.idMappingFile(localFolder),
          s3Obj = data.idMappingTSV(version)
        ).flatMap { idMappingsS3 =>
          // FASTA
          mirrorFile(
            url = new URL(data.input.speciesSpecificFASTAGZURL(version)),
            gzFile = data.local.fastaGZFile(localFolder),
            file = data.local.fastaFile(localFolder),
            s3Obj = data.speciesSpecificFASTA(version)
          ).map { fastaS3 =>
            Set(idMappingsS3, fastaS3)
          }
        }
      }

  }

  /**
    * Find any object under [[data.prefix(version)]] that could be overwritten
    * by [[mirrorNewVersion]].
    *
    * @param version is the version that specifies the S3 folder
    *
    * @return Some(object) with the first object found under
    * [[data.prefix(version)]] if any, None otherwise.
    */
  private def findObjectInS3(version: Version): Option[S3Object] =
    data
      .everything(version)
      .find(
        obj => s3Helpers.objectExists(obj).fold(_ => true, identity)
      )

  /**
    * Mirror a new version of RNACentral to S3 if and only if the upload does
    * not override anything.
    *
    * This method tries to download [[data.input.releaseURL]], uncompress it
    * and upload the corresponding files to the objects defined in
    * [[data.idMappingTSV]] and [[data.speciesSpecificFASTA]].
    *
    * It does so if and only if none of those two objects already exist in S3.
    * If any of them exists, nothing is downloaded nor uploaded and an error is
    * returned.
    *
    * @param version is the new version that wants to be released
    * @param localFolder is the localFolder where the downloaded files will be
    * stored.
    *
    * @return an Error + Set[S3Object], with a Right(set) with all the mirrored
    * S3 objects if everything worked as expected or with a Left(error) if an
    * error occurred. Several things could go wrong in this process; namely:
    *   - The objects already exist in S3
    *   - The input file could not be downloaded
    *   - The input file could not be uncompressed
    *   - The upload process failed, either because you have no permissions to
    *   upload to the objects under [[data.prefix]] or because some error
    *   occured during the upload itself.
    */
  def mirrorNewVersion(
      version: Version,
      localFolder: File
  ): Error + Set[S3Object] =
    findObjectInS3(version).fold(mirrorVersion(version, localFolder)) { obj =>
      Left(Error.S3ObjectExists(obj))
    }
}
