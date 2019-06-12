# Implementation Notes

## RNACentral FASTA files

The species-specific file is *not* sorted (as one would expect). I need to read the file line by line, keep the header lines, and add that info to a `Map[RNAID, ArrayList[FastaInfo]]`. For the sequences we add them if missing to another map of type `Map[RNAID, DNA]`.

## RNACentral tsv files

Again, not sorted. How on earth this is even possible. Same as for FASTA then. Parse each row, add to a `Map[RNAID, ArrayList[Row]]`.

## Memory Usage

The IDs take at most `100MB` IIRC so no big deal. Seqs and annotations could take `~2GB` each so it's possible to have everything in memory. The TSV info should be really small (basically references) maybe a bit more for the links which we can drop on a first step. All in all no more than `.5GB`.

Note that once we have a binary format for all this (almost trivial) we can iterate through entries/values of those maps easily.


