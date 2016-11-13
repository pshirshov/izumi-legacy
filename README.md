A simple Scala toolkit for fast evolving advanced applications.

## Publishing

    $ sbt updateBugfix
    $ sbt clean publish
    $ git checkout HEAD~1
    $ sbt clean publish sonatypeRelease
    $ git checkout master