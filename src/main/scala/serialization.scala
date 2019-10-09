package ohnosequences.db.rnacentral

import ohnosequences.std.io, io._
import ohnosequences.dna._

object read {

  def rstream(f: File): RStream = new RStream(BRStream(f))

  // DNA
  /////////////////////////////////////////////////////////////////////
  def dna(rs: RStream): DNA = {

    import ohnosequences.bits._

    val len = rs.readLong
    if (len <= 0) DNA.empty
    else {

      val numWords = BitVector.numWords(len)

      val left  = new BitVector(io.read.longArrayElements(rs, numWords), len)
      val right = new BitVector(io.read.longArrayElements(rs, numWords), len)

      new DNA(leftBits = left, rightBits = right)
    }
  }
  def dnaArray(rs: RStream): Array[DNA] = io.read.array(rs, dna _)

  // TaxID
  /////////////////////////////////////////////////////////////////////
  def taxID(rs: RStream): TaxID             = rs.readInt
  def taxIDArray(rs: RStream): Array[TaxID] = io.read.intArray(rs)

  // RNAID
  /////////////////////////////////////////////////////////////////////
  def rnaID(rs: RStream): RNAID             = rs.readLong
  def rnaIDArray(rs: RStream): Array[RNAID] = io.read.longArray(rs)

  // Database
  /////////////////////////////////////////////////////////////////////
  def database(rs: RStream): Database = Database from io.read.string(rs)

  // RNAType
  /////////////////////////////////////////////////////////////////////
  def rnaType(rs: RStream): RNAType = RNAType from io.read.string(rs)

  // Header
  /////////////////////////////////////////////////////////////////////
  def header(rs: RStream): Header = {

    val x = taxID(rs)
    val y = io.read.string(rs)

    new Header(x, y)
  }
  def headerArray(rs: RStream): Array[Header] = io.read.array(rs, header _)

  // RNAHeaders
  /////////////////////////////////////////////////////////////////////
  def rnaHeaders(rs: RStream): RNAHeaders = {

    val x = rnaID(rs)
    val y = headerArray(rs)

    new RNAHeaders(x, y)
  }
  def rnaHeadersArray(f: File): Array[RNAHeaders] =
    io.read.array(rstream(f), rnaHeaders)

  def rnaHeadersIterator(f: File): Iterator[RNAHeaders] =
    rnaHeadersIterator(rstream(f))
  def rnaHeadersIterator(rs: RStream): Iterator[RNAHeaders] =
    Iterator.fill(rs.readInt) { rnaHeaders(rs) }

  // Mapping
  /////////////////////////////////////////////////////////////////////
  def mapping(rs: RStream): Mapping = {

    val x = database(rs)
    val y = taxID(rs)
    val z = rnaType(rs)
    val w = io.read.string(rs)

    new Mapping(x, y, z, w)
  }
  def mappingArray(rs: RStream): Array[Mapping] =
    io.read.array(rs, mapping)

  // RNAMappings
  /////////////////////////////////////////////////////////////////////
  def rnaMappings(rs: RStream): RNAMappings = {

    val x = rnaID(rs)
    val y = mappingArray(rs)

    new RNAMappings(x, y)
  }
  def rnaMappingsArray(f: File): Array[RNAMappings] =
    io.read.array(rstream(f), rnaMappings)

  def rnaMappingsIterator(f: File): Iterator[RNAMappings] =
    rnaMappingsIterator(rstream(f))

  def rnaMappingsIterator(rs: RStream): Iterator[RNAMappings] =
    Iterator.fill(rs.readInt) { rnaMappings(rs) }

  // RNASequence
  /////////////////////////////////////////////////////////////////////
  def rnaSequence(rs: RStream): RNASequence = {

    val x = rnaID(rs)
    val y = dna(rs)
    RNASequence(x, y)
  }
  def rnaSequenceArray(rs: RStream): Array[RNASequence] =
    io.read.array(rs, rnaSequence)
  def rnaSequenceArray(f: File): Array[RNASequence] =
    rnaSequenceArray(rstream(f))
  def rnaSequenceIterator(rs: RStream): Iterator[RNASequence] =
    Iterator.fill(rs.readInt) { rnaSequence(rs) }
  def rnaSequenceIterator(f: File): Iterator[RNASequence] =
    rnaSequenceIterator(rstream(f))
}

object write {

  def wstream(f: File): WStream = new WStream(BWStream(f))

  // DNA
  /////////////////////////////////////////////////////////////////////
  def dna(x: DNA, ws: WStream): WStream = {

    ws writeLong x.length

    if (!x.empty) {
      io.write.longArrayElements(x.leftBits.words, ws)
      io.write.longArrayElements(x.rightBits.words, ws)
    }

    ws
  }
  def dnaArray(xs: Array[DNA], ws: WStream): WStream =
    io.write.array(xs, ws, dna _)

  // TaxID
  /////////////////////////////////////////////////////////////////////
  def taxID(x: TaxID, ws: WStream): WStream = {

    ws writeInt x
    ws
  }
  def taxIDArray(xs: Array[TaxID], ws: WStream): WStream =
    io.write.intArray(xs, ws)

  // RNAID
  /////////////////////////////////////////////////////////////////////
  def rnaID(x: RNAID, ws: WStream): WStream = {
    ws writeLong x
    ws
  }
  def rnaIDArray(xs: Array[RNAID], ws: WStream): WStream =
    io.write.longArray(xs, ws)

  // Header
  /////////////////////////////////////////////////////////////////////
  def header(x: Header, ws: WStream): WStream = {
    taxID(x.taxID, ws)
    io.write.string(x.text, ws)
  }
  def headerArray(xs: Array[Header], ws: WStream): WStream =
    io.write.array(xs, ws, header _)

  // RNAHeaders
  /////////////////////////////////////////////////////////////////////
  def rnaHeaders(x: RNAHeaders, ws: WStream): WStream = {
    rnaID(x.rnaID, ws)
    headerArray(x.headers, ws)
  }
  def rnaHeadersArray(xs: Array[RNAHeaders], ws: WStream): WStream =
    io.write.array(xs, ws, rnaHeaders _)
  def rnaHeadersArray(xs: Array[RNAHeaders], file: File): File = {
    rnaHeadersArray(xs, wstream(file)).close
    file
  }

  // Database
  /////////////////////////////////////////////////////////////////////
  def database(x: Database, ws: WStream): WStream = io.write.string(x.name, ws)

  // RNAType
  /////////////////////////////////////////////////////////////////////
  def rnaType(x: RNAType, ws: WStream): WStream = io.write.string(x.name, ws)

  // Mapping
  /////////////////////////////////////////////////////////////////////
  def mapping(x: Mapping, ws: WStream): WStream = {
    database(x.db, ws)
    taxID(x.taxID, ws)
    rnaType(x.rnaType, ws)
    io.write.string(x.geneName, ws)
  }
  def mappingArray(xs: Array[Mapping], ws: WStream): WStream =
    io.write.array(xs, ws, mapping _)

  // RNAMappings
  /////////////////////////////////////////////////////////////////////
  def rnaMappings(x: RNAMappings, ws: WStream): WStream = {
    rnaID(x.rnaID, ws)
    mappingArray(x.mappings, ws)
  }
  def rnaMappingsArray(xs: Array[RNAMappings], file: File): File = {
    rnaMappingsArray(xs, wstream(file)).close
    file
  }
  def rnaMappingsArray(xs: Array[RNAMappings], ws: WStream): WStream =
    io.write.array(xs, ws, rnaMappings _)

  // RNAMappings
  /////////////////////////////////////////////////////////////////////
  def rnaSequence(x: RNASequence, ws: WStream): WStream = {
    rnaID(x.rnaID, ws)
    dna(x.sequence, ws)
  }
  def rnaSequenceArray(xs: Array[RNASequence], ws: WStream): WStream =
    io.write.array(xs, ws, rnaSequence _)
  def rnaSequenceArray(xs: Array[RNASequence], f: File): File = {
    rnaSequenceArray(xs, wstream(f)).close
    f
  }
}
