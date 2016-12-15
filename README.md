A simple Scala toolkit for fast evolving advanced applications.

Publishing
----------

The workflow:

1. `sbt updateBugfix`
2. `git checkout HEAD~1`
3. `sbt -Dpb.strict-version=false clean publish sonatypeRelease`
4. `git checkout master`

Convenience oneliner:

    sbt -Dpb.config=perfect-build.json.SAMPLE updateBugfix && \
      git push --mirror && git checkout HEAD~1 && \
      sbt -Dpb.strict-version=false clean publish sonatypeRelease && \
      git checkout master
