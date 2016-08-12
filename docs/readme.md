## Maintainer docs

Making new releases of this project should be very easy:
- check in `project/plugins.sbt` that you're using the latest version of the [nice-sbt-settings](https://github.com/ohnosequences/nice-sbt-settings/releases/) plugin
- write release notes in `notes/changelog.md` (or directly `notes/<release_version>.markdown`) and commit
- open sbt and type `release `, then if you press <kbd>Tab</kbd>, you will see release version options, choose one and follow the release process

One thing to note here, is that data generation is a part of the release process in this project, namely it is a bundle which will be launched by a _release-only_ test after the artifacts are published.
