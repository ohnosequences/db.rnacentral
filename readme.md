# db.rnacentral

[![](https://travis-ci.org/ohnosequences/db.rnacentral.svg?branch=master)](https://travis-ci.org/ohnosequences/db.rnacentral)
[![](http://github-release-version.herokuapp.com/github/ohnosequences/db.rnacentral/release.svg)](https://github.com/ohnosequences/db.rnacentral/releases/latest)
[![](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)

`db.rnacentral` contains code to mirror the data from RNACentral releases, as well as pointers to the location of the data.

For each supported version of RNACentral, two files are available:

* RNAcentral Id Mappings —`id_mapping.tsv`—. "Tab-separated file with RNAcentral ids, corresponding external ids, NCBI taxon ids, RNA types (according to INSDC classification), and gene names"[¹](#references).
* RNAcentral Sequence Data —`rnacentral_species_specific_ids.fasta`—. "Current set of sequences that are present in at least one expert database using the species specific URS ID's"[²](#references).

# How to access the data

## Versions

All the data in `db.rnacentral` is versioned following the RNACentral releases number scheme.

Each of these versions is encoded as an object that extends the sealed class [`Version`](src/main/scala/data.scala#L7-L16).

The `Set` [`Version.all`](src/main/scala/data.scala#L12) contains all the releases supported and maintained through `db.rnacentral`.

## Files

The module [`db.rnacentral.data`](src/main/scala/data.scala#L18) contains the pointers to the S3 objects where the actual files are stored. The path of the S3 objects corresponding to the id mappings and the sequence data can be accessed evaluating the following functions over a `Version` object:

```scala
idMappingTSV         : Version => S3Object
speciesSpecificFASTA : Version => S3Object
```

A convenient value grouping both files can be accessed (again parametrized by the version) through the function:

```scala
everything : Version => S3Object
```

The path to the S3 objects returned by those functions something like the following:

```
s3://resources.ohnosequences.com/ohnosequences/db/rnacentral/<version>/id_mapping.tsv
s3://resources.ohnosequences.com/ohnosequences/db/rnacentral/<version>/rnacentral_species_specific_ids.fasta
```

---------------
#### References

1: ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral/releases/10.0/id_mapping/readme.txt

2: ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral/releases/10.0/id_mapping/readme.txt
