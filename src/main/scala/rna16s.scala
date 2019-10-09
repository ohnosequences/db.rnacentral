package ohnosequences.db.rnacentral

object rna16s {

  val has16SHeader: RNAHeaders => Boolean =
    xs =>
      xs.headers.exists { h =>
        (h.text contains "16S") || (h.text contains "16s")
    }

  def from(data: BinaryRNACentralData): Iterator[RNAHeaders] =
    read.rnaHeadersIterator(data.rnaHeaders) collect {
      case x if has16SHeader(x) => x
    }

  def idsFrom(data: BinaryRNACentralData): Iterator[RNAID] =
    from(data).map(_.rnaID)
}
