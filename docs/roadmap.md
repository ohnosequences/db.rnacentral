# Roadmap

## 0.3.0

### Persistence and serialization

Having two separate files which furthermore need to be kept in the same order is prone to all sort of errors; we also need to work easily with subsets of RNACentral as a coherent whole. We are going to have our own data serialization format.

- **[#6](/../../issues/6)** a csv spec where each row represents an `Entry`
- **[#7](/../../issues/7)** parse/serialize functions
- **[#8](/../../issues/8)** transform RNACentral `FASTA` + `tsv` into this csv format

### Documentation

- **[#5](/../../issues/5)** Scaladoc and example usage in tests

### Full test coverage

- **[#9](/../../issues/9)** This should include parsing the whole RNACentral distribution, and going back and forth to our csv format

## 1.0

**IMPORTANT** *Tentative/subject to change*

### A better data model

we have a good enough representation in db.rnacentral (see `data.scala`). We want to have a graph model for it.