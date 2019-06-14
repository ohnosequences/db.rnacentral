package ohnosequences.db.rnacentral

import ohnosequences.std.io, io._

object read {

  @inline final def taxID(rs: RStream): TaxID =
    rs.readInt

  @inline final def rnaID(rs: RStream): RNAID =
    rs.readLong: @inline

  @inline final def rnaIDArray(rs: RStream): Array[RNAID] =
    io.read.longArray(rs): @inline

  final def header(rs: RStream): Header = {

    val x = taxID(rs)
    val y = io.read.string(rs)

    new Header(x, y)
  }

  final def headerArray(rs: RStream): Array[Header] =
    io.read.array(rs, header _)

  final def rnaHeaders(rs: RStream): RNAHeaders = {

    val x = rnaID(rs)
    val y = headerArray(rs)

    new RNAHeaders(x, y)
  }

  final def database(rs: RStream): Database =
    Database from io.read.string(rs)

  final def rnaType(rs: RStream): RNAType =
    RNAType from io.read.string(rs)

  final def idMappingRow(rs: RStream): IDMappingRow = {

    val x = database(rs)
    val y = taxID(rs)
    val z = rnaType(rs)
    val w = io.read.string(rs)

    new IDMappingRow(x, y, z, w)
  }

  final def idMappingRowArray(rs: RStream): Array[IDMappingRow] =
    io.read.array(rs, idMappingRow)

  final def rnaMappings(rs: RStream): RNAMappings = {

    val x = rnaID(rs)
    val y = idMappingRowArray(rs)

    new RNAMappings(x, y)
  }

  final def rnaMappingsIterator(f: File): Iterator[RNAMappings] =
    rnaMappingsIterator(new RStream(BRStream(f)))

  final def rnaMappingsIterator(rs: RStream): Iterator[RNAMappings] =
    Iterator.fill(rs.readInt) { rnaMappings(rs) }
}

object write {

  @inline final def taxID(x: TaxID, ws: WStream): WStream = {

    ws writeInt x
    ws
  }

  final def rnaID(x: RNAID, ws: WStream): WStream = {
    ws writeLong x
    ws
  }

  final def rnaIDArray(xs: Array[RNAID], ws: WStream): WStream =
    io.write.longArray(xs, ws)

  final def header(x: Header, ws: WStream): WStream = {
    taxID(x.taxID, ws)
    io.write.string(x.text, ws)
  }

  final def headerArray(xs: Array[Header], ws: WStream): WStream =
    io.write.array(xs, ws, header _)

  final def rnaHeaders(x: RNAHeaders, ws: WStream): WStream = {
    rnaID(x.rnaID, ws)
    headerArray(x.headers, ws)
  }

  final def database(x: Database, ws: WStream): WStream =
    io.write.string(x.name, ws)

  final def rnaType(x: RNAType, ws: WStream): WStream =
    io.write.string(x.name, ws)

  final def idMappingRow(x: IDMappingRow, ws: WStream): WStream = {
    database(x.db, ws)
    taxID(x.taxID, ws)
    rnaType(x.rnaType, ws)
    io.write.string(x.geneName, ws)
  }

  final def idMappingRowArray(xs: Array[IDMappingRow], ws: WStream): WStream =
    io.write.array(xs, ws, idMappingRow _)

  final def rnaMappings(x: RNAMappings, ws: WStream): WStream = {
    rnaID(x.rnaID, ws)
    idMappingRowArray(x.mappings, ws)
  }
}
