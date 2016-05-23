### RNA reference databases

[![](https://travis-ci.org/era7bio/db.rnacentral.svg?branch=master)](https://travis-ci.org/era7bio/db.rnacentral)
[![](https://img.shields.io/codacy/???.svg)](https://www.codacy.com/app/era7bio/db.rnacentral)
[![](https://img.shields.io/badge/RNAcentral-v5.0-blue.svg)](http://blog.rnacentral.org/2016/03/rnacentral-release-5.html)
[![](http://github-release-version.herokuapp.com/github/era7bio/db.rnacentral/release.svg)](https://github.com/era7bio/db.rnacentral/releases/latest)
[![](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)
[![](https://img.shields.io/badge/contact-gitter_chat-dd1054.svg)](https://gitter.im/era7bio/db.rnacentral)

This is a bundle for mirroring [RNAcentral](http://rnacentral.org/) database to S3:

```
s3://resources.ohnosequences.com/era7bio/db.rnacentral/<RNAcental_version>/<release_version>/
```

(check these versions on the badges)

And also a library for building BLAST reference databases based on RNAcentral:

- [era7bio/db.rna16s](https://github.com/era7bio/db.rna16s)
- [era7bio/db.rna18s](https://github.com/era7bio/db.rna18s)
