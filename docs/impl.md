# Implementation Notes

In short:

1. Need id_mapping, species-specific fasta and active fasta
2. sort id_mapping, extract and sort headers from species-specific fasta
3. keep *only* the active ones: those in the id_mapping file
3. Store all this in binary format
4. add sequences from the active fasta

## RNACentral FASTA files

The species-specific file is *not* sorted (as one would expect). I'm extracting the headers, sorting them, and storing them in binary format.

### Headers

They are already there for `10.0`: the file is `headers.sorted`.

The binary representation is

```
[rnaid][header_size][taxid][text][taxid][text]...
  8B      4B          4B     ?     4B     ?
```

Headers will be ~60 chars length so `~120B` each. Around `2GB` for the whole DB.

### Sequences

The sequences will be added later from the standard FASTA file.

## RNACentral tsv files

Again, not sorted. How on earth this is even possible. Same as for FASTA then; sort it first.

## Memory Usage

The IDs take at most `100MB` IIRC so no big deal. Seqs and annotations could take `~2GB` each so it's possible to have everything in memory. The TSV info should be really small (basically references) maybe a bit more for the links which we can drop on a first step. All in all no more than `.5GB`.

Note that once we have a binary format for all this (almost trivial) we can iterate through entries/values of those maps easily.


