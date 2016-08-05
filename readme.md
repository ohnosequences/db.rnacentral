### RNA reference databases

[![](https://travis-ci.org/ohnosequences/db.rnacentral.svg?branch=master)](https://travis-ci.org/ohnosequences/db.rnacentral)
[![](https://img.shields.io/codacy/???.svg)](https://www.codacy.com/app/ohnosequences/db.rnacentral)
[![](https://img.shields.io/badge/RNAcentral-v5.0-blue.svg)](http://blog.rnacentral.org/2016/03/rnacentral-release-5.html)
[![](http://github-release-version.herokuapp.com/github/ohnosequences/db.rnacentral/release.svg)](https://github.com/ohnosequences/db.rnacentral/releases/latest)
[![](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)
[![](https://img.shields.io/badge/contact-gitter_chat-dd1054.svg)](https://gitter.im/ohnosequences/db.rnacentral)

This is a bundle for mirroring [RNAcentral](http://rnacentral.org/) database to S3:

```
s3://resources.ohnosequences.com/ohnosequences/db.rnacentral/<RNAcental_version>/<release_version>/
```

(check these versions on the badges)

And also a library for building BLAST reference databases based on RNAcentral:

- [ohnosequences/db.rna16s](https://github.com/ohnosequences/db.rna16s)
- [ohnosequences/db.rna18s](https://github.com/ohnosequences/db.rna18s)
