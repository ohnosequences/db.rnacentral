package ohnosequences.db.rnacentral

import it.unimi.dsi.fastutil.longs.LongOpenHashSet

object toBinary {

  def activeRNAIDs(input: RNACentralData): LongOpenHashSet =
    new LongOpenHashSet(RNAMappings.from(input).map(_.rnaID).toArray)

  def writeRNASequences(xs: Array[RNASequence], output: BinaryRNACentralData) =
    write.rnaSequenceArray(xs, output.rnaSequences)
  def writeRNAMappings(xs: Array[RNAMappings], output: BinaryRNACentralData) =
    write.rnaMappingsArray(xs, output.rnaMappings)
  def writeRNAHeaders(xs: Array[RNAHeaders], output: BinaryRNACentralData) =
    write.rnaHeadersArray(xs, output.rnaHeaders)

  def apply(
      input: RNACentralData,
      output: BinaryRNACentralData
  ): BinaryRNACentralData = {

    val active_ids = activeRNAIDs(input)

    // parse mappings, write to file, keep IDs in a Set
    writeRNAMappings(RNAMappings.from(input).toArray, output)
    writeRNAHeaders(RNAHeaders.from(input, active_ids).toArray, output)
    writeRNASequences(RNASequence.from(input, active_ids).toArray, output)
    output
  }
}

object fromBinary {

  def rnaHeadersIterator(input: BinaryRNACentralData) =
    read.rnaHeadersIterator(input.rnaHeaders)
  def rnaSequenceIterator(input: BinaryRNACentralData) =
    read.rnaSequenceIterator(input.rnaSequences)
  def rnaMappingsIterator(input: BinaryRNACentralData) =
    read.rnaMappingsIterator(input.rnaMappings)

  def rnaID2Sequence(xs: Iterator[RNASequence], size: Int) = {
    val map = new RNAID2Sequence(size)
    xs foreach { z =>
      map.put(z.rnaID, z.sequence)
    }
    map
  }

  def rnaID2Mappings(xs: Iterator[RNAMappings], size: Int) = {
    val map = new RNAID2Mappings(size)
    xs foreach { z =>
      map.put(z.rnaID, z.mappings)
    }
    map
  }

  def rnaID2Headers(xs: Iterator[RNAHeaders], size: Int) = {
    val map = new RNAID2Headers(size)
    xs foreach { z =>
      map.put(z.rnaID, z.headers)
    }
    map
  }

  def apply(input: BinaryRNACentralData,
            only: LongOpenHashSet): RNACentralSubset = {

    val len = only.size

    new RNACentralSubset(
      rnaID2Headers(
        rnaHeadersIterator(input).filter(x => only contains x.rnaID),
        len),
      rnaID2Mappings(
        rnaMappingsIterator(input).filter(x => only contains x.rnaID),
        len),
      rnaID2Sequence(
        rnaSequenceIterator(input).filter(x => only contains x.rnaID),
        len),
    )

  }

  def apply(input: BinaryRNACentralData): RNACentralSubset = {

    val len = read.rnaHeadersIterator(input.rnaHeaders).length
    println(s"length: ${len}")
    // sequences
    println("loading sequences")
    val seqs = new RNAID2Sequence(len)
    read.rnaSequenceIterator(input.rnaSequences) foreach { z =>
      seqs.put(z.rnaID, z.sequence)
    }
    // mappings
    println("loading mappings")
    val mappings = new RNAID2Mappings(len)
    read.rnaMappingsIterator(input.rnaMappings) foreach { z =>
      mappings.put(z.rnaID, z.mappings)
    }
    // headers
    println("loading headers")
    val headers = new RNAID2Headers(len)
    read.rnaHeadersIterator(input.rnaHeaders) foreach { z =>
      headers.put(z.rnaID, z.headers)
    }

    new RNACentralSubset(headers, mappings, seqs)
  }

}
