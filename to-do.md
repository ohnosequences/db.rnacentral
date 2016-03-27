## Assignment IDs to drop

- 115547 "uncultured archaeon"
- 155900 "uncultured organism"
- 358574 "uncultured microorganism"
- 77133 "uncultured"
- 344338 "uncultured firmicutes bacterium"

### RNACentral for taxonomic assignment

We use RNACentral as a starting point for building a RNA reference database suitable for taxonomic assignment. Why not use all the RNACentral sequences? our reasons are

1. small RNA sequences, sometimes contained in other sequences, tend to distort BLAST results
2. Sequences with dubious taxonomic assignments (uncultured bacterium and the like) are at best useless and at worst seriously impair the accuracy of a lowest common ancestor method.
